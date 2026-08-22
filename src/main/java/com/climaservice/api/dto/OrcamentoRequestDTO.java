package com.climaservice.api.dto;

import jakarta.validation.constraints.Size;

public record OrcamentoRequestDTO(

        @Size(
                max = 1000,
                message = "A observação deve possuir no máximo 1000 caracteres"
        )
        String observacao

) {
}