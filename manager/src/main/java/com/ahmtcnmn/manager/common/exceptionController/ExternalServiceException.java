package com.ahmtcnmn.manager.common.exceptionController;

import com.ahmtcnmn.manager.common.exception.MessageType;

public class ExternalServiceException extends AppException {

    public ExternalServiceException(MessageType messageType) {
        super(messageType);
    }

    public ExternalServiceException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
}
