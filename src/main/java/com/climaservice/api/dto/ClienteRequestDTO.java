package com.climaservice.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve possuir no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "O CPF/CNPJ é obrigatório")
        @Pattern(
                regexp = "\\d{11}|\\d{14}",
                message = "O CPF/CNPJ deve possuir 11 ou 14 dígitos"
        )
        String cpfCnpj,

        @Size(max = 20, message = "O telefone deve possuir no máximo 20 caracteres")
        String telefone,

        @Email(message = "Informe um e-mail válido")
        @Size(max = 150, message = "O e-mail deve possuir no máximo 150 caracteres")
        String email,

        @Valid
        EnderecoDTO endereco,

        @Size(max = 30, message = "A inscrição municipal deve possuir no máximo 30 caracteres")
        String inscricaoMunicipal,

        @Size(max = 30, message = "A inscrição estadual deve possuir no máximo 30 caracteres")
        String inscricaoEstadual

) {
}