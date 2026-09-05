/*
 * Garante a nível de banco a regra "no máximo uma nota fiscal ativa por
 * ordem de serviço" — a checagem em aplicação (exists antes do insert)
 * sozinha não fecha a corrida entre duas requisições concorrentes.
 */
CREATE UNIQUE INDEX uq_nota_fiscal_servico_ordem_servico_ativa
    ON nota_fiscal_servico(ordem_servico_id)
    WHERE status <> 'CANCELADA';
