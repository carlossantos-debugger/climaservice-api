package com.climaservice.api.dto;

import java.time.LocalDateTime;

public record EmpresaResponseDTO(

        Long id,
        String nome,
        String cpfCnpj,
        boolean ativo,
        LocalDateTime dataCriacao

) {
}
