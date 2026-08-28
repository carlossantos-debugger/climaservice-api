package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusAgendamento;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusAgendamentoRequestDTO(

        @NotNull(message = "O status é obrigatório")
        StatusAgendamento status

) {
}
