ALTER TABLE empresa
    ADD CONSTRAINT uk_empresa_cpf_cnpj UNIQUE (cpf_cnpj);
