package com.climaservice.api.dto;

import com.climaservice.api.entity.FormaPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PagamentoRequestDTO(

        @NotNull(message = "O valor do pagamento é obrigatório")
        @DecimalMin(
                value = "0.01",
                message = "O valor do pagamento deve ser maior que zero"
        )
        BigDecimal valor,

        @NotNull(message = "A forma de pagamento é obrigatória")
        FormaPagamento formaPagamento,

        @Size(
                max = 500,
                message = "A observação deve possuir no máximo 500 caracteres"
        )
        String observacao

) {
}