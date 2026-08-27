ALTER TABLE cliente
    ADD COLUMN empresa_id BIGINT;

UPDATE cliente
SET empresa_id = (
    SELECT id
    FROM empresa
    WHERE nome = 'Empresa Inicial'
    ORDER BY id
    LIMIT 1
    );

ALTER TABLE cliente
    ALTER COLUMN empresa_id SET NOT NULL;

ALTER TABLE cliente
    ADD CONSTRAINT fk_cliente_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id);

CREATE INDEX idx_cliente_empresa_id
    ON cliente(empresa_id);