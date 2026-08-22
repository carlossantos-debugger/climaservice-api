package com.climaservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarDiagnosticoRequestDTO(

        @NotBlank(message = "O diagnóstico é obrigatório")
        @Size(
                max = 2000,
                message = "O diagnóstico deve possuir no máximo 2000 caracteres"
        )
        String diagnostico

) {
}