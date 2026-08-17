package com.climaservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EquipamentoRequestDTO(

        @NotBlank(message = "A marca é obrigatória")
        @Size(max = 100, message = "A marca deve possuir no máximo 100 caracteres")
        String marca,

        @NotBlank(message = "O modelo é obrigatório")
        @Size(max = 100, message = "O modelo deve possuir no máximo 100 caracteres")
        String modelo,

        @NotNull(message = "A capacidade em BTUs é obrigatória")
        @Positive(message = "A capacidade em BTUs deve ser maior que zero")
        Integer capacidadeBtu,

        @Size(max = 100, message = "O número de série deve possuir no máximo 100 caracteres")
        String numeroSerie,

        @Size(max = 150, message = "O local de instalação deve possuir no máximo 150 caracteres")
        String localInstalacao,

        @NotNull(message = "O cliente é obrigatório")
        Long clienteId

) {
}