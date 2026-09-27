package com.ahmtcnmn.manager.common.exceptionController;

import org.springframework.http.HttpStatus;

import com.ahmtcnmn.manager.common.exception.ErrorMessage;
import com.ahmtcnmn.manager.common.exception.MessageType;

import lombok.Getter;

@Getter
public abstract class AppException extends RuntimeException {

    private final MessageType messageType;

    protected AppException(MessageType messageType) {
        super(new ErrorMessage(messageType).prepareErrorMessage());
        this.messageType = messageType;
    }

    protected AppException(MessageType messageType, String detail) {
        super(new ErrorMessage(messageType, detail).prepareErrorMessage());
        this.messageType = messageType;
    }

    public HttpStatus getHttpStatus() {
        return messageType.getHttpStatus();
    }

    public String getCode() {
        return messageType.name();
    }
}
