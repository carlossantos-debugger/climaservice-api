package com.climaservice.api.dto;

import com.climaservice.api.entity.RoleUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCadastroRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        @Size(
                max = 150,
                message = "O nome deve possuir no máximo 150 caracteres"
        )
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail informado é inválido")
        @Size(
                max = 150,
                message = "O e-mail deve possuir no máximo 150 caracteres"
        )
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(
                min = 8,
                max = 72,
                message = "A senha deve possuir entre 8 e 72 caracteres"
        )
        String senha,

        @NotNull(message = "O perfil do usuário é obrigatório")
        RoleUsuario role

) {
}