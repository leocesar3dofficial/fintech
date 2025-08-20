package com.leo.fintech.common.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.leo.fintech.common.exception.ErrorResponse;
import com.leo.fintech.common.exception.ErrorResponseUtil;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleServerErrors(Exception ex, WebRequest request) {
        logger.error("Unexpected server error occurred: ", ex);

        ErrorResponse errorResponse = ErrorResponseUtil.build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred while processing your request",
                request);

        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
