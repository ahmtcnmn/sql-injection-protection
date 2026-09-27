package com.ahmtcnmn.manager.common.exceptionController;

import com.ahmtcnmn.manager.common.exception.MessageType;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(MessageType messageType) {
        super(messageType);
    }

    public ResourceNotFoundException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
}
