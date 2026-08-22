package com.climaservice.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequestDTO(

        @NotBlank(message = "O nome do produto é obrigatório")
        @Size(
                max = 150,
                message = "O nome deve possuir no máximo 150 caracteres"
        )
        String nome,

        @Size(
                max = 500,
                message = "A descrição deve possuir no máximo 500 caracteres"
        )
        String descricao,

        @NotNull(message = "O valor padrão é obrigatório")
        @DecimalMin(
                value = "0.01",
                message = "O valor padrão deve ser maior que zero"
        )
        BigDecimal valorPadrao

) {
}