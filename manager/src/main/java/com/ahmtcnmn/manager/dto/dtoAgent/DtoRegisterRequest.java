package com.ahmtcnmn.manager.dto.dtoAgent;

import jakarta.validation.constraints.NotBlank;

public record DtoRegisterRequest(
    @NotBlank String hostName
) {}
