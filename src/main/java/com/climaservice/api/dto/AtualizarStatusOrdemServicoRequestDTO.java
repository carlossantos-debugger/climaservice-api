package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusOrdemServico;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusOrdemServicoRequestDTO(

        @NotNull(message = "O status é obrigatório")
        StatusOrdemServico status

) {
}