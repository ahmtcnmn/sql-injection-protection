package com.ahmtcnmn.manager.dto.Command;

import jakarta.validation.constraints.NotBlank;

public record DtoCommandRequest(
    @NotBlank String commandType,
    String payload
) {}
