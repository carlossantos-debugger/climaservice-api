package com.climaservice.api.integration;

import com.climaservice.api.entity.Pagamento;
import com.climaservice.api.entity.StatusPagamento;
import com.climaservice.api.repository.PagamentoRepository;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PagamentoRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {

        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    pagamento_historico,
                    pagamento,
                    orcamento_historico,
                    orcamento_item,
                    orcamento,
                    ordem_servico_diagnostico_historico,
                    ordem_servico_historico,
                    ordem_servico,
                    equipamento,
                    cliente,
                    usuario,
                    empresa
                RESTART IDENTITY CASCADE
                """);

        jdbcTemplate.update("""
                INSERT INTO empresa (
                    id,
                    nome,
                    cpf_cnpj,
                    ativo,
                    data_criacao
                )
                VALUES (
                    8001,
                    'ClimaService Teste',
                    NULL,
                    true,
                    CURRENT_TIMESTAMP
                )
                """);

        jdbcTemplate.update("""
                INSERT INTO cliente (
                    id,
                    nome,
                    cpf_cnpj,
                    telefone,
                    email,
                    empresa_id
                )
                VALUES (
                    1001,
                    'Cliente Teste',
                    '12345678901',
                    '47999999999',
                    'cliente@teste.com',
                    8001
                )
                """);

        jdbcTemplate.update("""
                INSERT INTO equipamento (
                    id,
                    capacidade_btu,
                    local_instalacao,
                    marca,
                    modelo,
                    numero_serie,
                    cliente_id,
                    status
                )
                VALUES (
                    2001,
                    12000,
                    'Sala',
                    'LG',
                    'Dual Inverter',
                    'SERIE-001',
                    1001,
                    'ATIVO'
                )
                """);

        jdbcTemplate.update("""
                INSERT INTO ordem_servico (
                    id,
                    data_abertura,
                    descricao_problema,
                    diagnostico,
                    status,
                    cliente_id,
                    equipamento_id
                )
                VALUES (
                    3001,
                    CURRENT_TIMESTAMP,
                    'Equipamento não está resfriando',
                    NULL,
                    'ABERTA',
                    1001,
                    2001
                )
                """);

        jdbcTemplate.update("""
                INSERT INTO orcamento (
                    id,
                    data_criacao,
                    data_envio,
                    data_resposta,
                    observacao,
                    status,
                    valor_total,
                    ordem_servico_id
                )
                VALUES (
                    4001,
                    CURRENT_TIMESTAMP,
                    NULL,
                    NULL,
                    'Orçamento teste',
                    'APROVADO',
                    1000.00,
                    3001
                )
                """);

        jdbcTemplate.update("""
                INSERT INTO pagamento (
                    id,
                    data_criacao,
                    data_confirmacao,
                    data_cancelamento,
                    forma_pagamento,
                    observacao,
                    status,
                    valor,
                    orcamento_id
                )
                VALUES (
                    5001,
                    '2026-08-25 10:00:00',
                    NULL,
                    NULL,
                    'PIX',
                    'Primeiro pagamento',
                    'PENDENTE',
                    250.00,
                    4001
                )
                """);

        jdbcTemplate.update("""
                INSERT INTO pagamento (
                    id,
                    data_criacao,
                    data_confirmacao,
                    data_cancelamento,
                    forma_pagamento,
                    observacao,
                    status,
                    valor,
                    orcamento_id
                )
                VALUES (
                    5002,
                    '2026-08-25 11:00:00',
                    CURRENT_TIMESTAMP,
                    NULL,
                    'DINHEIRO',
                    'Segundo pagamento',
                    'CONFIRMADO',
                    400.00,
                    4001
                )
                """);
    }

    @Test
    void deveBuscarPagamentosPendentesDoOrcamento() {

        List<Pagamento> pagamentos = pagamentoRepository.findByOrcamentoIdAndStatus(4001L, StatusPagamento.PENDENTE);

        assertEquals(1, pagamentos.size());

        Pagamento pagamento = pagamentos.get(0);

        assertEquals(StatusPagamento.PENDENTE, pagamento.getStatus());

        assertEquals(0, new BigDecimal("250.00").compareTo(pagamento.getValor()));

        assertEquals(4001L, pagamento.getOrcamento().getId());
    }

    @Test
    void deveListarPagamentosDoOrcamentoEmOrdemDeCriacao() {

        List<Pagamento> pagamentos = pagamentoRepository.findByOrcamentoIdOrderByDataCriacaoAsc(4001L);

        assertEquals(2, pagamentos.size());

        assertEquals(5001L, pagamentos.get(0).getId());

        assertEquals(5002L, pagamentos.get(1).getId());

        assertEquals(StatusPagamento.PENDENTE, pagamentos.get(0).getStatus());

        assertEquals(StatusPagamento.CONFIRMADO, pagamentos.get(1).getStatus());
    }
}