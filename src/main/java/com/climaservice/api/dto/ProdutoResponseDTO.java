package com.climaservice.api.dto;

import java.math.BigDecimal;

public record ProdutoResponseDTO(

        Long id,
        String nome,
        String descricao,
        BigDecimal valorPadrao,
        Boolean ativo

) {
}