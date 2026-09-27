package com.ahmtcnmn.manager.dto.Event;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DtoEventRequest(
    @NotBlank String eventType,
    @Min(1) @Max(5) Integer severity,
    String rawData,
    String sourceIp
) {}
