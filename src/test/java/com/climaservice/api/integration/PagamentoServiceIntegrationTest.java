package com.climaservice.api.integration;

import com.climaservice.api.dto.PagamentoRequestDTO;
import com.climaservice.api.dto.PagamentoResponseDTO;
import com.climaservice.api.entity.FormaPagamento;
import com.climaservice.api.entity.Pagamento;
import com.climaservice.api.entity.PagamentoHistorico;
import com.climaservice.api.entity.StatusPagamento;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.repository.PagamentoHistoricoRepository;
import com.climaservice.api.repository.PagamentoRepository;
import com.climaservice.api.service.PagamentoService;

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
class PagamentoServiceIntegrationTest extends AbstractIntegrationTest {

    private static final Long EMPRESA_ID = 8001L;
    private static final Long ORCAMENTO_ID = 4001L;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private PagamentoHistoricoRepository historicoRepository;

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
                INSERT INTO usuario (
                    id,
                    ativo,
                    data_criacao,
                    email,
                    nome,
                    role,
                    senha_hash,
                    empresa_id
                )
                VALUES (
                    9001,
                    true,
                    CURRENT_TIMESTAMP,
                    'admin@teste.com',
                    'Administrador Teste',
                    'ADMIN',
                    'hash-teste',
                    8001
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
                    status,
                    empresa_id
                )
                VALUES (
                    2001,
                    12000,
                    'Sala',
                    'LG',
                    'Dual Inverter',
                    'SERIE-001',
                    1001,
                    'ATIVO',
                    8001
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
                    equipamento_id,
                    empresa_id
                )
                VALUES (
                    3001,
                    CURRENT_TIMESTAMP,
                    'Equipamento não está resfriando',
                    NULL,
                    'ABERTA',
                    1001,
                    2001,
                    8001
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
                    ordem_servico_id,
                    empresa_id
                )
                VALUES (
                    4001,
                    CURRENT_TIMESTAMP,
                    NULL,
                    NULL,
                    'Orçamento de teste',
                    'APROVADO',
                    1000.00,
                    3001,
                    8001
                )
                """);

        autenticarUsuario();
    }

    private void autenticarUsuario() {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("admin@teste.com", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveCriarEConfirmarPagamentoComPersistenciaReal() {

        // Arrange
        PagamentoRequestDTO dto = new PagamentoRequestDTO(new BigDecimal("250.00"), FormaPagamento.PIX, "Pagamento integração");

        // Act - criação
        PagamentoResponseDTO criado = pagamentoService.criar(ORCAMENTO_ID, dto);

        // Assert - criação
        assertNotNull(criado);
        assertNotNull(criado.id());

        assertEquals(StatusPagamento.PENDENTE, criado.status());

        assertEquals(0, new BigDecimal("250.00").compareTo(criado.valor()));

        Pagamento pagamentoPersistido = pagamentoRepository.findByIdAndOrcamento_Empresa_Id(criado.id(), EMPRESA_ID).orElseThrow();

        assertEquals(StatusPagamento.PENDENTE, pagamentoPersistido.getStatus());

        List<PagamentoHistorico> historicoCriacao = historicoRepository.findByPagamentoIdOrderByDataAlteracaoAsc(criado.id());

        assertEquals(1, historicoCriacao.size());

        assertNull(historicoCriacao.get(0).getStatusAnterior());

        assertEquals(StatusPagamento.PENDENTE, historicoCriacao.get(0).getStatusNovo());

        assertNotNull(historicoCriacao.get(0).getUsuario());

        assertEquals(9001L, historicoCriacao.get(0).getUsuario().getId());

        // Act - confirmação
        PagamentoResponseDTO confirmado = pagamentoService.confirmar(criado.id());

        // Assert - confirmação
        assertEquals(StatusPagamento.CONFIRMADO, confirmado.status());

        assertNotNull(confirmado.dataConfirmacao());

        Pagamento pagamentoConfirmado = pagamentoRepository.findByIdAndOrcamento_Empresa_Id(criado.id(), EMPRESA_ID).orElseThrow();

        assertEquals(StatusPagamento.CONFIRMADO, pagamentoConfirmado.getStatus());

        assertNotNull(pagamentoConfirmado.getDataConfirmacao());

        List<PagamentoHistorico> historicoCompleto = historicoRepository.findByPagamentoIdOrderByDataAlteracaoAsc(criado.id());

        assertEquals(2, historicoCompleto.size());

        PagamentoHistorico segundaAlteracao = historicoCompleto.get(1);

        assertEquals(StatusPagamento.PENDENTE, segundaAlteracao.getStatusAnterior());

        assertEquals(StatusPagamento.CONFIRMADO, segundaAlteracao.getStatusNovo());

        assertEquals(9001L, segundaAlteracao.getUsuario().getId());
    }

    @Test
    void deveImpedirPagamentoAcimaDoSaldoUsandoDadosReaisDoBanco() {

        // Arrange
        PagamentoRequestDTO primeiroPagamento = new PagamentoRequestDTO(new BigDecimal("900.00"), FormaPagamento.PIX, "Pagamento inicial");

        pagamentoService.criar(ORCAMENTO_ID, primeiroPagamento);

        PagamentoRequestDTO pagamentoAcimaDoSaldo = new PagamentoRequestDTO(new BigDecimal("150.00"), FormaPagamento.PIX, "Pagamento inválido");

        // Act + Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> pagamentoService.criar(ORCAMENTO_ID, pagamentoAcimaDoSaldo));

        assertEquals("O valor do pagamento ultrapassa o saldo disponível de 100.00", exception.getMessage());

        List<Pagamento> pagamentos = pagamentoRepository.findByOrcamento_IdAndOrcamento_Empresa_IdOrderByDataCriacaoAsc(ORCAMENTO_ID, EMPRESA_ID);

        assertEquals(1, pagamentos.size());

        assertEquals(0, new BigDecimal("900.00").compareTo(pagamentos.get(0).getValor()));
    }
}