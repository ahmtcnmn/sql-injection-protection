package com.ahmtcnmn.manager.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum MessageType {

    AGENT_NOT_FOUND("Agent not found", HttpStatus.NOT_FOUND),
    AGENT_ALREADY_EXISTS("Agent already exists", HttpStatus.CONFLICT),
    INVALID_API_KEY("Invalid API key", HttpStatus.UNAUTHORIZED),
    MISSING_API_KEY("Missing API key", HttpStatus.UNAUTHORIZED),
    AGENT_OFFLINE("Agent is offline", HttpStatus.CONFLICT),

    INVALID_EVENT_TYPE("Invalid event type", HttpStatus.BAD_REQUEST),
    INVALID_SEVERITY_LEVEL("Invalid severity level", HttpStatus.BAD_REQUEST),
    EVENT_VALIDATION_FAILED("Event validation failed", HttpStatus.BAD_REQUEST),

    COMMAND_NOT_FOUND("Command not found", HttpStatus.NOT_FOUND),
    COMMAND_EXECUTION_FAILED("Command execution failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_COMMAND_PARAMETERS("Invalid command parameters", HttpStatus.BAD_REQUEST),

    ACCESS_DENIED("Access denied", HttpStatus.FORBIDDEN),
    TENANT_MISMATCH("Tenant mismatch", HttpStatus.FORBIDDEN),

    RATE_LIMIT_EXCEEDED("Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_ERROR("External service error", HttpStatus.BAD_GATEWAY),
    EXTERNAL_SERVICE_UNAVAILABLE("External service unavailable", HttpStatus.SERVICE_UNAVAILABLE);

    private final String message;
    private final HttpStatus httpStatus;

    MessageType(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
