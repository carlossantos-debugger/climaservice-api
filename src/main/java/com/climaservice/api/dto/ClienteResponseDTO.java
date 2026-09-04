package com.climaservice.api.dto;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        EnderecoDTO endereco,
        String inscricaoMunicipal,
        String inscricaoEstadual
) {
}
