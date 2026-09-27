package com.ahmtcnmn.manager.common.exception;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path) {

    public static ErrorResponse of(HttpStatus httpStatus, String code, String message, String path) {
        return new ErrorResponse(Instant.now(), httpStatus.value(), httpStatus.getReasonPhrase(), code, message, path);
    }

    public static ErrorResponse of(HttpStatus httpStatus, String code, List<String> messages, String path) {
        return of(httpStatus, code, String.join("; ", messages), path);
    }
}
