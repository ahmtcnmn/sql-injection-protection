package com.ahmtcnmn.manager.dto.Command;

import java.time.Instant;

import com.ahmtcnmn.manager.model.Command;

public record DtoCommandResponse(
    Long id,
    String commandType,
    String payload,
    String status,
    String result,
    Instant createdAt,
    Instant completedAt
) {
    public static DtoCommandResponse fromEntity(Command command) {
        return new DtoCommandResponse(
            command.getId(),
            command.getCommandType(),
            command.getPayload(),
            command.getStatus().name(),
            command.getResult(),
            command.getCreatedAt(),
            command.getCompletedAt()
        );
    }
}
