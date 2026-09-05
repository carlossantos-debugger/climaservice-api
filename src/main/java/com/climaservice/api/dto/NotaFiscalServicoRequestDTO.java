package com.climaservice.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record NotaFiscalServicoRequestDTO(

        @NotBlank(message = "A discriminação do serviço é obrigatória")
        @Size(max = 1000, message = "A discriminação do serviço deve possuir no máximo 1000 caracteres")
        String discriminacaoServico,

        @Size(max = 20, message = "O código de serviço deve possuir no máximo 20 caracteres")
        String codigoServico,

        @DecimalMin(value = "0", message = "A alíquota de ISS deve ser maior ou igual a zero")
        @DecimalMax(value = "100", message = "A alíquota de ISS deve ser menor ou igual a 100")
        BigDecimal aliquotaIss

) {
}
