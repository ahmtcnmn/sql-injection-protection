package com.ahmtcnmn.manager.common.exceptionController;

import com.ahmtcnmn.manager.common.exception.MessageType;

public class CommandExecutionException extends AppException {

    public CommandExecutionException(MessageType messageType) {
        super(messageType);
    }

    public CommandExecutionException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
}
