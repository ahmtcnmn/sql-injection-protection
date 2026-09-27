package com.ahmtcnmn.manager.common.exceptionController;

import com.ahmtcnmn.manager.common.exception.MessageType;

public class UnauthorizedException extends AppException {

    public UnauthorizedException(MessageType messageType) {
        super(messageType);
    }

    public UnauthorizedException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
}
