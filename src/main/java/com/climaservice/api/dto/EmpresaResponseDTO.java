package com.climaservice.api.dto;

import com.climaservice.api.entity.RegimeTributario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EmpresaResponseDTO(

        Long id,
        String nome,
        String cpfCnpj,
        boolean ativo,
        LocalDateTime dataCriacao,
        EnderecoDTO endereco,
        String inscricaoMunicipal,
        RegimeTributario regimeTributario,
        String codigoServicoPadrao,
        BigDecimal aliquotaIssPadrao

) {
}
