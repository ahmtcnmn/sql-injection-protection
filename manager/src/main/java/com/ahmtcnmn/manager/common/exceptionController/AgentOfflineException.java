package com.ahmtcnmn.manager.common.exceptionController;

import com.ahmtcnmn.manager.common.exception.MessageType;

public class AgentOfflineException extends AppException {

    public AgentOfflineException(MessageType messageType) {
        super(messageType);
    }

    public AgentOfflineException(MessageType messageType, String detail) {
        super(messageType, detail);
    }

    
}
