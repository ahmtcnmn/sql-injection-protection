package com.ahmtcnmn.manager.common.exceptionController;

import com.ahmtcnmn.manager.common.exception.MessageType;

public class ValidationException extends AppException {

    public ValidationException(MessageType messageType) {
        super(messageType);
    }

    public ValidationException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
}
