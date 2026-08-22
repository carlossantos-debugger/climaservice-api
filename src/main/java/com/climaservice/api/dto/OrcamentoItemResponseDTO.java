package com.climaservice.api.dto;

import com.climaservice.api.entity.TipoItemOrcamento;

import java.math.BigDecimal;

public record OrcamentoItemResponseDTO(

        Long id,

        TipoItemOrcamento tipo,

        Long servicoId,

        String descricao,

        Integer quantidade,

        BigDecimal valorUnitario,

        BigDecimal subtotal

) {
}