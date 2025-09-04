package com.leo.fintech.common.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.leo.fintech.common.exception.ErrorResponse;
import com.leo.fintech.common.exception.ErrorResponseUtil;

@RestControllerAdvice
@Order(400)
public class DatabaseExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseExceptionHandler.class);

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ErrorResponse> handleJpaSystemException(JpaSystemException ex, WebRequest request) {
        logger.error("JPA System Exception occurred: ", ex);

        HttpStatus status;
        String message;
        String details;

        if (ex.getMessage() != null && ex.getMessage().contains("identifier of an instance")) {
            status = HttpStatus.BAD_REQUEST;
            message = "Missing or invalid ID for update operation";
            details = "When updating a resource, the ID must be provided";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Database operation failed";
            details = "An unexpected database error occurred";
        }

        ErrorResponse errorResponse = ErrorResponseUtil.build(status, message, details, request);
        return ResponseEntity.status(status).body(errorResponse);
    }
}
