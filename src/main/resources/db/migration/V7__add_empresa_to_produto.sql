ALTER TABLE produto
    ADD COLUMN empresa_id BIGINT;

UPDATE produto
SET empresa_id = (
    SELECT MIN(id)
    FROM empresa
);

ALTER TABLE produto
    ALTER COLUMN empresa_id SET NOT NULL;

ALTER TABLE produto
    ADD CONSTRAINT fk_produto_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id);

CREATE INDEX idx_produto_empresa_id
    ON produto(empresa_id);