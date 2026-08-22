package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusOrcamento;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusOrcamentoRequestDTO(

        @NotNull(message = "O status é obrigatório")
        StatusOrcamento status

) {
}