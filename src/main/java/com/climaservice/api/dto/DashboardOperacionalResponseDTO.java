package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusOrdemServico;

import java.util.List;
import java.util.Map;

public record DashboardOperacionalResponseDTO(

        Map<StatusOrdemServico, Long> osPorStatus,
        long osConcluidas,
        long osCanceladas,
        List<AgendamentoResponseDTO> proximosAgendamentos,
        List<PlanoManutencaoPreventivaResponseDTO> manutencoesPreventivasProximas

) {
}
