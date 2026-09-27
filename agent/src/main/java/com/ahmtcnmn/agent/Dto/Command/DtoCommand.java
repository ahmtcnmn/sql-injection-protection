package com.ahmtcnmn.agent.Dto.Command;

import java.time.Instant;

public record DtoCommand(
    Long id,
    String commandType,
    String payload,
    String status,
    Instant createdAt,
    Instant completedAt,
    String result
) {}