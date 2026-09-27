package com.ahmtcnmn.agent.Dto.responder;

import lombok.Builder;

@Builder
public record DtoEventRequest(
    String eventType,
    Integer severity,
    String rawData,
    String sourceIp
) {}