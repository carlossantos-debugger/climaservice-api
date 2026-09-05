package com.climaservice.api.dto;

import com.climaservice.api.entity.Endereco;

/*
 * Conversão Endereco <-> EnderecoDTO compartilhada entre os services que
 * expõem endereço (Cliente, Empresa) — evita duplicar a mesma lógica em
 * cada um.
 */
public final class EnderecoMapper {

    private EnderecoMapper() {
    }

    public static Endereco paraEntidade(EnderecoDTO dto) {

        if (dto == null) {

            return null;
        }

        return new Endereco(dto.logradouro(), dto.numero(), dto.complemento(), dto.bairro(), dto.cidade(), dto.uf(), dto.cep());
    }

    public static EnderecoDTO paraDTO(Endereco endereco) {

        if (endereco == null) {

            return null;
        }

        return new EnderecoDTO(endereco.getLogradouro(), endereco.getNumero(), endereco.getComplemento(), endereco.getBairro(), endereco.getCidade(), endereco.getUf(), endereco.getCep());
    }
}
