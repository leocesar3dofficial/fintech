package com.leo.fintech.common.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

public class ErrorResponseUtil {

    public static ErrorResponse build(HttpStatus status, String message, String details, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                details,
                path);
    }
}
