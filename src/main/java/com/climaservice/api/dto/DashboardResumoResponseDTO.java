package com.climaservice.api.dto;

import java.math.BigDecimal;

public record DashboardResumoResponseDTO(

        long clientesAtivos,
        long equipamentosAtivos,
        long ordensAbertas,
        long ordensEmAndamento,
        long orcamentosPendentes,
        long agendamentosHoje,
        BigDecimal receitaConfirmadaNoMes

) {
}
