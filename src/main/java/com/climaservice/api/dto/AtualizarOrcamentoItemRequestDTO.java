package com.climaservice.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AtualizarOrcamentoItemRequestDTO(

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade,

        @DecimalMin(
                value = "0.01",
                message = "O valor unitário deve ser maior que zero"
        )
        BigDecimal valorUnitario

) {
}