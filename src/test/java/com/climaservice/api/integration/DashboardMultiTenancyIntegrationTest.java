package com.climaservice.api.integration;

import com.climaservice.api.dto.DashboardFinanceiroResponseDTO;
import com.climaservice.api.dto.DashboardOperacionalResponseDTO;
import com.climaservice.api.dto.DashboardResumoResponseDTO;
import com.climaservice.api.entity.StatusOrdemServico;
import com.climaservice.api.service.DashboardService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DashboardMultiTenancyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {

        jdbcTemplate.execute("""
                TRUNCATE TABLE empresa
                RESTART IDENTITY CASCADE
                """);

        // Empresas
        jdbcTemplate.update("""
                INSERT INTO empresa (id, nome, cpf_cnpj, ativo, data_criacao)
                VALUES (8001, 'Empresa A', NULL, true, CURRENT_TIMESTAMP),
                       (8002, 'Empresa B', NULL, true, CURRENT_TIMESTAMP)
                """);

        // Usuários
        jdbcTemplate.update("""
                INSERT INTO usuario (id, ativo, data_criacao, email, nome, role, senha_hash, empresa_id)
                VALUES
                    (9001, true, CURRENT_TIMESTAMP, 'admin-a@teste.com', 'Administrador A', 'ADMIN', 'hash', 8001),
                    (9002, true, CURRENT_TIMESTAMP, 'tecnico-a@teste.com', 'Técnico A', 'TECNICO', 'hash', 8001),
                    (9101, true, CURRENT_TIMESTAMP, 'admin-b@teste.com', 'Administrador B', 'ADMIN', 'hash', 8002),
                    (9102, true, CURRENT_TIMESTAMP, 'tecnico-b@teste.com', 'Técnico B', 'TECNICO', 'hash', 8002)
                """);

        // Clientes
        jdbcTemplate.update("""
                INSERT INTO cliente (id, nome, cpf_cnpj, telefone, email, empresa_id)
                VALUES (1001, 'Cliente A1', '11111111111', '47911111111', 'cliente-a1@teste.com', 8001),
                       (1002, 'Cliente A2', '11111111112', '47911111112', 'cliente-a2@teste.com', 8001),
                       (2001, 'Cliente B1', '22222222221', '47922222221', 'cliente-b1@teste.com', 8002)
                """);

        // Equipamentos
        jdbcTemplate.update("""
                INSERT INTO equipamento (id, capacidade_btu, local_instalacao, marca, modelo, numero_serie, cliente_id, status, empresa_id)
                VALUES (3001, 12000, 'Sala', 'LG', 'Dual Inverter', 'SERIE-A1', 1001, 'ATIVO', 8001),
                       (3002, 9000, 'Sala', 'LG', 'Inverter', 'SERIE-A2', 1002, 'INATIVO', 8001),
                       (4001, 12000, 'Sala', 'Samsung', 'WindFree', 'SERIE-B1', 2001, 'ATIVO', 8002)
                """);

        // Ordens de Serviço (Empresa A: 1 ABERTA, 1 EM_ANDAMENTO, 1 CONCLUIDA, 1 CANCELADA)
        jdbcTemplate.update("""
                INSERT INTO ordem_servico (id, data_abertura, descricao_problema, diagnostico, status, cliente_id, equipamento_id, empresa_id)
                VALUES (5001, CURRENT_TIMESTAMP, 'Problema A1', NULL, 'ABERTA', 1001, 3001, 8001),
                       (5002, CURRENT_TIMESTAMP, 'Problema A2', NULL, 'EM_ANDAMENTO', 1001, 3001, 8001),
                       (5003, CURRENT_TIMESTAMP, 'Problema A3', NULL, 'CONCLUIDA', 1002, 3002, 8001),
                       (5004, CURRENT_TIMESTAMP, 'Problema A4', NULL, 'CANCELADA', 1001, 3001, 8001),
                       (6001, CURRENT_TIMESTAMP, 'Problema B1', NULL, 'ABERTA', 2001, 4001, 8002)
                """);

        // Orçamentos (Empresa A: 1 ENVIADO, 2 APROVADO, 1 RASCUNHO)
        jdbcTemplate.update("""
                INSERT INTO orcamento (id, data_criacao, data_envio, data_resposta, observacao, status, valor_total, ordem_servico_id, empresa_id)
                VALUES (7001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, NULL, 'ENVIADO', 1000.00, 5001, 8001),
                       (7002, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 'APROVADO', 2000.00, 5002, 8001),
                       (7003, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 'APROVADO', 1000.00, 5003, 8001),
                       (7004, CURRENT_TIMESTAMP, NULL, NULL, NULL, 'RASCUNHO', 0.00, 5004, 8001),
                       (7101, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, NULL, 'ENVIADO', 500.00, 6001, 8002)
                """);

        // Pagamentos (tenant derivado via orcamento -> empresa)
        // Empresa A: confirmado neste mês (2000+1000=3000), confirmado mês passado (300, não entra na receita do mês),
        // pendente (500). valorRecebido (todo o tempo) = 3300.
        jdbcTemplate.update("""
                INSERT INTO pagamento (id, data_cancelamento, data_confirmacao, data_criacao, forma_pagamento, observacao, status, valor, orcamento_id)
                VALUES (8101, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PIX', NULL, 'CONFIRMADO', 2000.00, 7002),
                       (8102, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PIX', NULL, 'CONFIRMADO', 1000.00, 7003),
                       (8103, NULL, (DATE_TRUNC('month', CURRENT_TIMESTAMP) - INTERVAL '1 day'), CURRENT_TIMESTAMP, 'PIX', NULL, 'CONFIRMADO', 300.00, 7002),
                       (8104, NULL, NULL, CURRENT_TIMESTAMP, 'BOLETO', NULL, 'PENDENTE', 500.00, 7001),
                       (8201, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PIX', NULL, 'CONFIRMADO', 500.00, 7101)
                """);

        // Agendamentos: hoje (ativo), hoje (cancelado, não deve contar), próximos 7 dias, fora da janela de 7 dias
        /*
         * data_hora_inicio precisa ficar no futuro em relação ao instante em
         * que o serviço roda LocalDateTime.now() (alguns milissegundos após
         * este INSERT) para contar como "próximo" — por isso +30 minutos em
         * vez de CURRENT_TIMESTAMP exato.
         */
        jdbcTemplate.update("""
                INSERT INTO agendamento (id, data_hora_inicio, data_hora_fim, status, observacao, data_criacao, ordem_servico_id, tecnico_id, empresa_id)
                VALUES (10001, CURRENT_TIMESTAMP + INTERVAL '30 minutes', CURRENT_TIMESTAMP + INTERVAL '90 minutes', 'AGENDADO', 'Hoje ativo', CURRENT_TIMESTAMP, 5001, 9002, 8001),
                       (10002, CURRENT_TIMESTAMP + INTERVAL '30 minutes', CURRENT_TIMESTAMP + INTERVAL '90 minutes', 'CANCELADO', 'Hoje cancelado', CURRENT_TIMESTAMP, 5001, 9002, 8001),
                       (10003, CURRENT_TIMESTAMP + INTERVAL '3 days', CURRENT_TIMESTAMP + INTERVAL '3 days 1 hour', 'CONFIRMADO', 'Proximo', CURRENT_TIMESTAMP, 5002, 9002, 8001),
                       (10004, CURRENT_TIMESTAMP + INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '10 days 1 hour', 'AGENDADO', 'Fora da janela', CURRENT_TIMESTAMP, 5002, 9002, 8001),
                       (10101, CURRENT_TIMESTAMP + INTERVAL '30 minutes', CURRENT_TIMESTAMP + INTERVAL '90 minutes', 'AGENDADO', 'Hoje B', CURRENT_TIMESTAMP, 6001, 9102, 8002)
                """);

        // Planos de manutenção preventiva
        jdbcTemplate.update("""
                INSERT INTO plano_manutencao_preventiva (id, intervalo_meses, proxima_execucao, ultima_execucao, ativo, observacao, data_criacao, equipamento_id, tecnico_padrao_id, empresa_id)
                VALUES (11001, 6, CURRENT_DATE + 10, NULL, true, 'Plano A', CURRENT_TIMESTAMP, 3001, NULL, 8001),
                       (11101, 3, CURRENT_DATE + 10, NULL, true, 'Plano B', CURRENT_TIMESTAMP, 4001, NULL, 8002)
                """);

        autenticar("admin-a@teste.com", "ADMIN");
    }

    @AfterEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar(String email, String role) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    @Test
    void deveCalcularResumoSomenteComDadosDaEmpresaA() {

        DashboardResumoResponseDTO resumo = dashboardService.obterResumo();

        assertEquals(2L, resumo.clientesAtivos());

        assertEquals(1L, resumo.equipamentosAtivos());

        assertEquals(1L, resumo.ordensAbertas());

        assertEquals(1L, resumo.ordensEmAndamento());

        assertEquals(1L, resumo.orcamentosPendentes());

        assertEquals(1L, resumo.agendamentosHoje());

        assertEquals(0, new BigDecimal("3000.00").compareTo(resumo.receitaConfirmadaNoMes()));
    }

    @Test
    void deveCalcularFinanceiroSomenteComDadosDaEmpresaA() {

        DashboardFinanceiroResponseDTO financeiro = dashboardService.obterFinanceiro();

        assertEquals(0, new BigDecimal("3300.00").compareTo(financeiro.valorRecebido()));

        assertEquals(0, new BigDecimal("500.00").compareTo(financeiro.valorPendente()));

        assertEquals(0, new BigDecimal("1500.00").compareTo(financeiro.ticketMedio()));

        assertEquals(2L, financeiro.quantidadeOrcamentosAprovados());
    }

    @Test
    void deveCalcularOperacionalSomenteComDadosDaEmpresaA() {

        DashboardOperacionalResponseDTO operacional = dashboardService.obterOperacional();

        assertEquals(1L, operacional.osPorStatus().get(StatusOrdemServico.ABERTA));

        assertEquals(1L, operacional.osPorStatus().get(StatusOrdemServico.EM_ANDAMENTO));

        assertEquals(1L, operacional.osConcluidas());

        assertEquals(1L, operacional.osCanceladas());

        /*
         * Janela de 7 dias inclui o agendamento de hoje (ativo) e o de
         * daqui a 3 dias, mas exclui o cancelado de hoje e o de 10 dias.
         */
        assertEquals(2, operacional.proximosAgendamentos().size());

        assertTrue(operacional.proximosAgendamentos().stream().noneMatch(a -> a.id().equals(10002L)));

        assertTrue(operacional.proximosAgendamentos().stream().noneMatch(a -> a.id().equals(10004L)));

        assertEquals(1, operacional.manutencoesPreventivasProximas().size());

        assertEquals(11001L, operacional.manutencoesPreventivasProximas().get(0).id());
    }

    @Test
    void deveMudarIsolamentoQuandoUsuarioDaEmpresaBForAutenticado() {

        autenticar("admin-b@teste.com", "ADMIN");

        DashboardResumoResponseDTO resumo = dashboardService.obterResumo();

        assertEquals(1L, resumo.clientesAtivos());

        assertEquals(1L, resumo.equipamentosAtivos());

        assertEquals(1L, resumo.ordensAbertas());

        assertEquals(0L, resumo.ordensEmAndamento());

        assertEquals(1L, resumo.orcamentosPendentes());

        assertEquals(1L, resumo.agendamentosHoje());

        DashboardFinanceiroResponseDTO financeiro = dashboardService.obterFinanceiro();

        assertEquals(0, new BigDecimal("500.00").compareTo(financeiro.valorRecebido()));

        assertEquals(0, BigDecimal.ZERO.compareTo(financeiro.valorPendente()));

        DashboardOperacionalResponseDTO operacional = dashboardService.obterOperacional();

        assertEquals(1, operacional.manutencoesPreventivasProximas().size());

        assertEquals(11101L, operacional.manutencoesPreventivasProximas().get(0).id());
    }
}
