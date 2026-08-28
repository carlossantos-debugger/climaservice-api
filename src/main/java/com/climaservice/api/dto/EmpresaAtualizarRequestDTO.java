package com.climaservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpresaAtualizarRequestDTO(

        @NotBlank(message = "O nome da empresa é obrigatório")
        @Size(max = 150, message = "O nome da empresa deve possuir no máximo 150 caracteres")
        String nome,

        @Pattern(
                regexp = "\\d{11}|\\d{14}",
                message = "O CPF/CNPJ deve possuir 11 ou 14 dígitos"
        )
        String cpfCnpj

) {
}
