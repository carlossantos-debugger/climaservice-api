package com.climaservice.api.dto;

import com.climaservice.api.entity.RoleUsuario;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(

        Long id,

        String nome,

        String email,

        RoleUsuario role,

        Boolean ativo,

        LocalDateTime dataCriacao

) {
}