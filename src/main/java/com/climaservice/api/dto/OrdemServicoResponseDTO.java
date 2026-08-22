package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusOrdemServico;

import java.time.LocalDateTime;

public record OrdemServicoResponseDTO(

        Long id,

        Long clienteId,
        String clienteNome,

        Long equipamentoId,
        String equipamentoMarca,
        String equipamentoModelo,

        String descricaoProblema,
        String diagnostico,

        StatusOrdemServico status,

        LocalDateTime dataAbertura,
        LocalDateTime dataConclusao

) {
}