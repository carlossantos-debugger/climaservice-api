package com.climaservice.api.dto;

import java.math.BigDecimal;

public record ServicoResponseDTO(

        Long id,
        String nome,
        String descricao,
        BigDecimal valorPadrao,
        Boolean ativo

) {
}