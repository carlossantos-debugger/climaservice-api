package com.climaservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrdemServicoRequestDTO(

        @NotNull(message = "O cliente é obrigatório")
        Long clienteId,

        @NotNull(message = "O equipamento é obrigatório")
        Long equipamentoId,

        @NotBlank(message = "A descrição do problema é obrigatória")
        @Size(
                max = 1000,
                message = "A descrição do problema deve possuir no máximo 1000 caracteres"
        )
        String descricaoProblema

) {
}