package com.climaservice.api.dto;

import com.climaservice.api.entity.RoleUsuario;

public record RegisterCompanyResponseDTO(

        Long empresaId,
        String empresaNome,
        Long usuarioId,
        String usuarioNome,
        String usuarioEmail,
        RoleUsuario role,
        String token

) {
}
