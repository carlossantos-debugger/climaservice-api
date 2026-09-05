package com.climaservice.api.dto;

import jakarta.validation.constraints.Size;

public record EnderecoDTO(

        @Size(max = 150, message = "O logradouro deve possuir no máximo 150 caracteres")
        String logradouro,

        @Size(max = 20, message = "O número deve possuir no máximo 20 caracteres")
        String numero,

        @Size(max = 100, message = "O complemento deve possuir no máximo 100 caracteres")
        String complemento,

        @Size(max = 100, message = "O bairro deve possuir no máximo 100 caracteres")
        String bairro,

        @Size(max = 100, message = "A cidade deve possuir no máximo 100 caracteres")
        String cidade,

        @Size(min = 2, max = 2, message = "A UF deve possuir 2 caracteres")
        String uf,

        @Size(max = 10, message = "O CEP deve possuir no máximo 10 caracteres")
        String cep

) {
}
