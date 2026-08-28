package com.climaservice.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record RegisterCompanyRequestDTO(

        @NotBlank(message = "O nome da empresa é obrigatório")
        @Size(max = 150, message = "O nome da empresa deve possuir no máximo 150 caracteres")
        String empresaNome,

        @Pattern(
                regexp = "\\d{11}|\\d{14}",
                message = "O CPF/CNPJ da empresa deve possuir 11 ou 14 dígitos"
        )
        String empresaCpfCnpj,

        @NotBlank(message = "O nome do administrador é obrigatório")
        @Size(max = 150, message = "O nome do administrador deve possuir no máximo 150 caracteres")
        String adminNome,

        @NotBlank(message = "O e-mail do administrador é obrigatório")
        @Email(message = "O e-mail do administrador informado é inválido")
        @Size(max = 150, message = "O e-mail do administrador deve possuir no máximo 150 caracteres")
        String adminEmail,

        @NotBlank(message = "A senha do administrador é obrigatória")
        @Size(min = 8, max = 72, message = "A senha do administrador deve possuir entre 8 e 72 caracteres")
        String adminSenha

) {
    public RegisterCompanyRequestDTO {
        if (adminEmail != null) {
            adminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        }
    }
}
