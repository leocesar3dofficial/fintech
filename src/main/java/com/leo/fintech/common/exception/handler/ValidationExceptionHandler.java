package com.leo.fintech.common.exception.handler;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.leo.fintech.common.exception.ErrorResponse;
import com.leo.fintech.common.exception.ErrorResponseUtil;

@RestControllerAdvice
public class ValidationExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionHandler.class);

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex, WebRequest request) {
                logger.warn("Invalid JSON request: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponseUtil.build(
                                HttpStatus.BAD_REQUEST,
                                "Invalid request data",
                                "Invalid JSON format or data type mismatch in request body",
                                request);

                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler({ MethodArgumentTypeMismatchException.class, IllegalArgumentException.class })
        public ResponseEntity<ErrorResponse> handleClientErrors(Exception ex, WebRequest request) {
                logger.warn("Client error occurred: {}", ex.getMessage());

                String details = (ex instanceof MethodArgumentTypeMismatchException)
                                ? "Invalid parameter type in request URL or query string"
                                : "Please check your request format and data types";

                ErrorResponse errorResponse = ErrorResponseUtil.build(
                                HttpStatus.BAD_REQUEST,
                                "Invalid request data",
                                details,
                                request);

                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,
                        WebRequest request) {
                String details = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining("; "));

                ErrorResponse errorResponse = ErrorResponseUtil.build(
                                HttpStatus.BAD_REQUEST,
                                "Validation failed",
                                details,
                                request);

                return ResponseEntity.badRequest().body(errorResponse);
        }
}
