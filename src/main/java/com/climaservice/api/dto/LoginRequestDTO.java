package com.climaservice.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

public record LoginRequestDTO(

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail informado é inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha

) {
    public LoginRequestDTO{
        if (email != null){
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
}