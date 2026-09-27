package com.ahmtcnmn.manager.dto.User;

import jakarta.validation.constraints.NotBlank;

public record DtoLoginRequest(
    @NotBlank(message = "Kullanıcı adı boş olamaz")
    String username,
    @NotBlank(message = "Şifre boş olamaz")
    String password
) {

}
