package com.climaservice.api.dto;

import com.climaservice.api.entity.FormaPagamento;
import com.climaservice.api.entity.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseDTO(

        Long id,

        Long orcamentoId,

        BigDecimal valor,

        FormaPagamento formaPagamento,

        StatusPagamento status,

        LocalDateTime dataCriacao,

        LocalDateTime dataConfirmacao,

        LocalDateTime dataCancelamento,

        String observacao

) {
}