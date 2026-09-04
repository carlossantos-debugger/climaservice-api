package com.climaservice.api.dto;

import com.climaservice.api.entity.AmbienteNotaFiscal;
import com.climaservice.api.entity.StatusNotaFiscalServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NotaFiscalServicoResponseDTO(

        Long id,

        Long ordemServicoId,
        Long orcamentoId,

        StatusNotaFiscalServico status,
        AmbienteNotaFiscal ambiente,

        String discriminacaoServico,
        String codigoServico,
        BigDecimal aliquotaIss,

        BigDecimal valorServico,
        BigDecimal valorIss,

        String numeroNota,
        String codigoVerificacao,
        String motivoRejeicao,
        LocalDateTime dataEmissao,

        String payloadMontado,

        LocalDateTime dataCriacao

) {
}
