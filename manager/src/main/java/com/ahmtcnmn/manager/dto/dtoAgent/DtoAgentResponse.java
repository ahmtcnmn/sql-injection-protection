package com.ahmtcnmn.manager.dto.dtoAgent;

import java.time.Instant;

import com.ahmtcnmn.manager.model.Agent;

public record DtoAgentResponse(
    Long id,
    String hostname,
    boolean isOnline,
    Instant lastSeen,
    Instant createdAt
) {
    public static DtoAgentResponse from(Agent agent) {
        return new DtoAgentResponse(
            agent.getId(),
            agent.getHostname(),
            agent.isOnline(),
            agent.getLastSeen(),
            agent.getCreatedAt()
        );
    }
}