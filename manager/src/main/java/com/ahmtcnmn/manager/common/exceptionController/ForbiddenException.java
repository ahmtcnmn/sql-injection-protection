package com.ahmtcnmn.manager.common.exceptionController;

import com.ahmtcnmn.manager.common.exception.MessageType;

public class ForbiddenException extends AppException {

    public ForbiddenException(MessageType messageType) {
        super(messageType);
    }

    public ForbiddenException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
}
