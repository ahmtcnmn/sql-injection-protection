package com.ahmtcnmn.manager.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {

    private MessageType messageType;
    private String detail;

    public ErrorMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    public String prepareErrorMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append(messageType.getMessage());
        if (detail != null && !detail.isEmpty()) {
            builder.append(" : ").append(detail);
        }
        return builder.toString();
    }
}
