package com.climaservice.api.dto;

import java.math.BigDecimal;

public record PagamentoResumoResponseDTO(

        Long orcamentoId,

        BigDecimal valorTotal,

        BigDecimal totalPago,

        BigDecimal saldoRestante

) {
}