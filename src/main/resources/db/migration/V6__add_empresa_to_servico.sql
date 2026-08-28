ALTER TABLE servico
    ADD COLUMN empresa_id BIGINT;

UPDATE servico
SET empresa_id = (
    SELECT MIN(id)
    FROM empresa
);

ALTER TABLE servico
    ALTER COLUMN empresa_id SET NOT NULL;

ALTER TABLE servico
    ADD CONSTRAINT fk_servico_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id);

CREATE INDEX idx_servico_empresa_id
    ON servico(empresa_id);