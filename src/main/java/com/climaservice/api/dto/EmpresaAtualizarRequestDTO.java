package com.climaservice.api.dto;

import com.climaservice.api.entity.RegimeTributario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record EmpresaAtualizarRequestDTO(

        @NotBlank(message = "O nome da empresa é obrigatório")
        @Size(max = 150, message = "O nome da empresa deve possuir no máximo 150 caracteres")
        String nome,

        @Pattern(
                regexp = "\\d{11}|\\d{14}",
                message = "O CPF/CNPJ deve possuir 11 ou 14 dígitos"
        )
        String cpfCnpj,

        @Valid
        EnderecoDTO endereco,

        @Size(max = 30, message = "A inscrição municipal deve possuir no máximo 30 caracteres")
        String inscricaoMunicipal,

        RegimeTributario regimeTributario,

        @Size(max = 20, message = "O código de serviço deve possuir no máximo 20 caracteres")
        String codigoServicoPadrao,

        @DecimalMin(value = "0", message = "A alíquota de ISS deve ser maior ou igual a zero")
        @DecimalMax(value = "100", message = "A alíquota de ISS deve ser menor ou igual a 100")
        BigDecimal aliquotaIssPadrao

) {
}
