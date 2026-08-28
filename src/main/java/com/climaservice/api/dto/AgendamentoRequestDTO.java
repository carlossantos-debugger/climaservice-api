package com.climaservice.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AgendamentoRequestDTO(

        @NotNull(message = "A ordem de serviço é obrigatória")
        Long ordemServicoId,

        @NotNull(message = "O técnico é obrigatório")
        Long tecnicoId,

        @NotNull(message = "A data/hora de início é obrigatória")
        LocalDateTime dataHoraInicio,

        @NotNull(message = "A data/hora de fim é obrigatória")
        LocalDateTime dataHoraFim,

        @Size(max = 1000, message = "A observação deve possuir no máximo 1000 caracteres")
        String observacao

) {
}
