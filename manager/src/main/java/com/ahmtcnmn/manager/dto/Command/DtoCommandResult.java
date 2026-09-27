package com.ahmtcnmn.manager.dto.Command;

import jakarta.validation.constraints.NotBlank;

public record DtoCommandResult(
    @NotBlank String status,
    String result
) {}
