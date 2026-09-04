package com.climaservice.api.dto;

import java.math.BigDecimal;

public record DashboardFinanceiroResponseDTO(

        BigDecimal valorRecebido,
        BigDecimal valorPendente,
        BigDecimal ticketMedio,
        long quantidadeOrcamentosAprovados

) {
}
