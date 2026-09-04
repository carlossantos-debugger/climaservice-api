-- Índices de apoio aos novos filtros e paginação (Branch 4 - feature/query-pagination).
-- Cobrem tanto as colunas usadas nos novos filtros quanto chaves estrangeiras
-- que ainda não possuíam índice e passam a ser usadas em joins de Specifications.

CREATE INDEX idx_cliente_nome
    ON cliente(nome);

CREATE INDEX idx_cliente_cpf_cnpj
    ON cliente(cpf_cnpj);

CREATE INDEX idx_equipamento_cliente_id
    ON equipamento(cliente_id);

CREATE INDEX idx_equipamento_status
    ON equipamento(status);

CREATE INDEX idx_equipamento_marca
    ON equipamento(marca);

CREATE INDEX idx_equipamento_modelo
    ON equipamento(modelo);

CREATE INDEX idx_ordem_servico_status
    ON ordem_servico(status);

CREATE INDEX idx_ordem_servico_cliente_id
    ON ordem_servico(cliente_id);

CREATE INDEX idx_ordem_servico_equipamento_id
    ON ordem_servico(equipamento_id);

CREATE INDEX idx_orcamento_status
    ON orcamento(status);

CREATE INDEX idx_orcamento_ordem_servico_id
    ON orcamento(ordem_servico_id);

CREATE INDEX idx_pagamento_status
    ON pagamento(status);

CREATE INDEX idx_pagamento_forma_pagamento
    ON pagamento(forma_pagamento);

CREATE INDEX idx_pagamento_orcamento_id
    ON pagamento(orcamento_id);
