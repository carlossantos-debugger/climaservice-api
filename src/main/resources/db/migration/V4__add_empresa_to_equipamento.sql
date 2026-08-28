ALTER TABLE equipamento
    ADD COLUMN empresa_id BIGINT;

UPDATE equipamento e
SET empresa_id = c.empresa_id
    FROM cliente c
WHERE e.cliente_id = c.id;

ALTER TABLE equipamento
    ALTER COLUMN empresa_id SET NOT NULL;

ALTER TABLE equipamento
    ADD CONSTRAINT fk_equipamento_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id);

CREATE INDEX idx_equipamento_empresa_id
    ON equipamento(empresa_id);