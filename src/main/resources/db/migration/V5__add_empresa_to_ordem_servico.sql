ALTER TABLE ordem_servico
    ADD COLUMN empresa_id BIGINT;

UPDATE ordem_servico os
SET empresa_id = c.empresa_id
    FROM cliente c
WHERE os.cliente_id = c.id;

ALTER TABLE ordem_servico
    ALTER COLUMN empresa_id SET NOT NULL;

ALTER TABLE ordem_servico
    ADD CONSTRAINT fk_ordem_servico_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id);

CREATE INDEX idx_ordem_servico_empresa_id
    ON ordem_servico(empresa_id);