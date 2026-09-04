-- As tabelas de histórico e orcamento_item são sempre consultadas por
-- sua chave estrangeira "pai" (findByXIdOrderBy...), mas nunca
-- ganharam índice nessa coluna nas migrations originais. Postgres não
-- indexa FKs automaticamente.

CREATE INDEX idx_ordem_servico_historico_ordem_servico_id
    ON ordem_servico_historico(ordem_servico_id);

CREATE INDEX idx_ordem_servico_diagnostico_historico_ordem_servico_id
    ON ordem_servico_diagnostico_historico(ordem_servico_id);

CREATE INDEX idx_orcamento_item_orcamento_id
    ON orcamento_item(orcamento_id);

CREATE INDEX idx_orcamento_historico_orcamento_id
    ON orcamento_historico(orcamento_id);

CREATE INDEX idx_pagamento_historico_pagamento_id
    ON pagamento_historico(pagamento_id);

CREATE INDEX idx_agendamento_historico_agendamento_id
    ON agendamento_historico(agendamento_id);
