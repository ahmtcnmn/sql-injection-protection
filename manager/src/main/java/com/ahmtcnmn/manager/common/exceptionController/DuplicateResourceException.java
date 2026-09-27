package com.ahmtcnmn.manager.common.exceptionController;

import com.ahmtcnmn.manager.common.exception.MessageType;

public class DuplicateResourceException extends AppException {

    public DuplicateResourceException(MessageType messageType) {
        super(messageType);
    }

    public DuplicateResourceException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
}
