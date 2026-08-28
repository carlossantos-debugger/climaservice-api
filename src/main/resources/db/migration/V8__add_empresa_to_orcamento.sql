ALTER TABLE orcamento
    ADD COLUMN empresa_id BIGINT;

UPDATE orcamento o
SET empresa_id = os.empresa_id
    FROM ordem_servico os
WHERE o.ordem_servico_id = os.id;

ALTER TABLE orcamento
    ALTER COLUMN empresa_id SET NOT NULL;

ALTER TABLE orcamento
    ADD CONSTRAINT fk_orcamento_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id);

CREATE INDEX idx_orcamento_empresa_id
    ON orcamento(empresa_id);