package com.leo.fintech.common.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.leo.fintech.common.exception.ErrorResponse;
import com.leo.fintech.common.exception.ErrorResponseUtil;
import com.leo.fintech.common.exception.InvalidPasswordException;
import com.leo.fintech.common.exception.InvalidTokenException;
import com.leo.fintech.common.exception.UserNotFoundException;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponseUtil.build(
                HttpStatus.BAD_REQUEST,
                "Invalid authentication token",
                ex.getMessage(),
                request);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponseUtil.build(
                HttpStatus.BAD_REQUEST,
                "Invalid password",
                ex.getMessage(),
                request);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponseUtil.build(
                HttpStatus.NOT_FOUND,
                "User not found",
                ex.getMessage(),
                request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}
