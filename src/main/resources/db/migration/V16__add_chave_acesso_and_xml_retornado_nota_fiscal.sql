ALTER TABLE nota_fiscal_servico
    ADD COLUMN chave_acesso VARCHAR(50),
    ADD COLUMN nfse_xml_retornado TEXT;

CREATE UNIQUE INDEX uq_nota_fiscal_servico_chave_acesso
    ON nota_fiscal_servico(chave_acesso)
    WHERE chave_acesso IS NOT NULL;
