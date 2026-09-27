package com.ahmtcnmn.manager.common.exceptionController;

import com.ahmtcnmn.manager.common.exception.MessageType;

public class AgentNotFoundException extends AppException {

    public AgentNotFoundException(MessageType messageType) {
        super(messageType);
    }

    public AgentNotFoundException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
    
}
