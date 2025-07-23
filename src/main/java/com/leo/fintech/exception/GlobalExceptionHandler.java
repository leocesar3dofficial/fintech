package com.leo.fintech.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityManager;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<Map<String, Object>> handleClientErrors(
            Exception ex, WebRequest request) {

        logger.warn("Client error occurred: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request data",
                "Please check your request format and data types",
                request);

        if (ex instanceof HttpMessageNotReadableException) {
            errorResponse.put("details", "Invalid JSON format or data type mismatch in request body");
        } else if (ex instanceof MethodArgumentNotValidException) {
            errorResponse.put("details", "Request validation failed - check required fields and constraints");
        } else if (ex instanceof MethodArgumentTypeMismatchException) {
            errorResponse.put("details", "Invalid parameter type in request URL or query string");
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<Map<String, Object>> handleJpaSystemException(
            JpaSystemException ex, WebRequest request) {

        logger.error("JPA System Exception occurred: ", ex);

        if (ex.getMessage() != null && ex.getMessage().contains("identifier of an instance")) {
            Map<String, Object> errorResponse = createErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Missing or invalid ID for update operation",
                    "When updating a resource, the ID must be provided",
                    request);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } else {
            Map<String, Object> errorResponse = createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database operation failed",
                    "An unexpected database error occurred",
                    request);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleServerErrors(
            Exception ex, WebRequest request) {

        logger.error("Unexpected server error occurred: ", ex);

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred while processing your request",
                request);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> createErrorResponse(
            HttpStatus status, String message, String details, WebRequest request) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("details", details);
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        return errorResponse;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body("Malformed JSON request");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("email", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}

@Component
class EntityStateHelper {

    private static final Logger logger = LoggerFactory.getLogger(EntityStateHelper.class);

    @Autowired
    private EntityManager entityManager;

    public <T> T safeSave(JpaRepository<T, ?> repository, T entity) {
        try {
            if (entityManager.contains(entity)) {
                logger.debug("Entity is already managed, performing merge operation");
                return entityManager.merge(entity);
            } else {
                logger.debug("Entity is not managed, performing save operation");
                return repository.save(entity);
            }
        } catch (Exception e) {
            logger.error("Error during safe save operation", e);
            throw new RuntimeException("Failed to save entity safely", e);
        }
    }

    public <T> boolean isManaged(T entity) {
        return entityManager.contains(entity);
    }

    public <T> void detach(T entity) {
        if (entityManager.contains(entity)) {
            entityManager.detach(entity);
        }
    }

}