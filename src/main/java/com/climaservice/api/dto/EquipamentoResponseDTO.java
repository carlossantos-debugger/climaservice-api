package com.climaservice.api.dto;

import com.climaservice.api.entity.StatusEquipamento;

public record EquipamentoResponseDTO(

        Long id,
        String marca,
        String modelo,
        Integer capacidadeBtu,
        String numeroSerie,
        String localInstalacao,
        StatusEquipamento status,
        Long clienteId,
        String clienteNome

) {
}