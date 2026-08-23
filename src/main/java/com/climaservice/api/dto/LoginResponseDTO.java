package com.climaservice.api.dto;

import com.climaservice.api.entity.RoleUsuario;

public record LoginResponseDTO(

        Long usuarioId,
        String nome,
        String email,
        RoleUsuario role

) {
}