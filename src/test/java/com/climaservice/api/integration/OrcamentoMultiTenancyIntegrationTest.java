package com.climaservice.api.integration;

import com.climaservice.api.dto.*;
import com.climaservice.api.entity.StatusOrcamento;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.service.OrcamentoService;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class OrcamentoMultiTenancyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrcamentoService orcamentoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {

        jdbcTemplate.execute("""
                TRUNCATE TABLE empresa
                RESTART IDENTITY CASCADE
                """);

        /*
         * Empresas
         */
        jdbcTemplate.update("""
                INSERT INTO empresa (
                    id,
                    nome,
                    cpf_cnpj,
                    ativo,
                    data_criacao
                )
                VALUES
                    (
                        8001,
                        'Empresa A',
                        NULL,
                        true,
                        CURRENT_TIMESTAMP
                    ),
                    (
                        8002,
                        'Empresa B',
                        NULL,
                        true,
                        CURRENT_TIMESTAMP
                    )
                """);

        /*
         * Usuários
         */
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
                VALUES
                    (
                        9001,
                        true,
                        CURRENT_TIMESTAMP,
                        'admin-a@teste.com',
                        'Administrador Empresa A',
                        'ADMIN',
                        'hash-teste',
                        8001
                    ),
                    (
                        9002,
                        true,
                        CURRENT_TIMESTAMP,
                        'admin-b@teste.com',
                        'Administrador Empresa B',
                        'ADMIN',
                        'hash-teste',
                        8002
                    )
                """);

        /*
         * Clientes
         */
        jdbcTemplate.update("""
                INSERT INTO cliente (
                    id,
                    nome,
                    cpf_cnpj,
                    telefone,
                    email,
                    empresa_id
                )
                VALUES
                    (
                        1001,
                        'Cliente A',
                        '11111111111',
                        '47911111111',
                        'cliente-a@teste.com',
                        8001
                    ),
                    (
                        2001,
                        'Cliente B',
                        '22222222222',
                        '47922222222',
                        'cliente-b@teste.com',
                        8002
                    )
                """);

        /*
         * Equipamentos
         */
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
                VALUES
                    (
                        3001,
                        12000,
                        'Sala A',
                        'LG',
                        'Dual Inverter',
                        'SERIE-A',
                        1001,
                        'ATIVO',
                        8001
                    ),
                    (
                        4001,
                        18000,
                        'Sala B',
                        'Samsung',
                        'WindFree',
                        'SERIE-B',
                        2001,
                        'ATIVO',
                        8002
                    )
                """);

        /*
         * Ordens de Serviço
         */
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
                VALUES
                    (
                        5001,
                        CURRENT_TIMESTAMP,
                        'Problema Empresa A',
                        NULL,
                        'ABERTA',
                        1001,
                        3001,
                        8001
                    ),
                    (
                        6001,
                        CURRENT_TIMESTAMP,
                        'Problema Empresa B',
                        NULL,
                        'ABERTA',
                        2001,
                        4001,
                        8002
                    )
                """);

        /*
         * Serviços
         */
        jdbcTemplate.update("""
                INSERT INTO servico (
                    id,
                    nome,
                    descricao,
                    valor_padrao,
                    ativo,
                    empresa_id
                )
                VALUES
                    (
                        7001,
                        'Limpeza Empresa A',
                        'Limpeza preventiva',
                        150.00,
                        true,
                        8001
                    ),
                    (
                        7002,
                        'Limpeza Empresa B',
                        'Limpeza preventiva',
                        180.00,
                        true,
                        8002
                    )
                """);

        /*
         * Produtos
         */
        jdbcTemplate.update("""
                INSERT INTO produto (
                    id,
                    nome,
                    descricao,
                    valor_padrao,
                    ativo,
                    empresa_id
                )
                VALUES
                    (
                        7101,
                        'Capacitor Empresa A',
                        'Capacitor para equipamento',
                        80.00,
                        true,
                        8001
                    ),
                    (
                        7102,
                        'Capacitor Empresa B',
                        'Capacitor para equipamento',
                        90.00,
                        true,
                        8002
                    )
                """);

        /*
         * Orçamentos
         */
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
                VALUES
                    (
                        7201,
                        CURRENT_TIMESTAMP,
                        NULL,
                        NULL,
                        'Orçamento Empresa A',
                        'RASCUNHO',
                        0.00,
                        5001,
                        8001
                    ),
                    (
                        7202,
                        CURRENT_TIMESTAMP,
                        NULL,
                        NULL,
                        'Orçamento Empresa B',
                        'RASCUNHO',
                        0.00,
                        6001,
                        8002
                    )
                """);

        autenticar("admin-a@teste.com", "ADMIN");
    }

    @AfterEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void devePermitirBuscarOrcamentoDaPropriaEmpresa() {

        OrcamentoResponseDTO resultado = orcamentoService.buscarPorId(7201L);

        assertNotNull(resultado);

        assertEquals(7201L, resultado.id());

        assertEquals(5001L, resultado.ordemServicoId());
    }

    @Test
    void deveImpedirBuscarOrcamentoDeOutraEmpresa() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> orcamentoService.buscarPorId(7202L));

        assertEquals("Orçamento com ID 7202 não encontrado", exception.getMessage());
    }

    @Test
    void deveListarSomenteOrcamentosDaOrdemDaEmpresaAtual() {

        List<OrcamentoResponseDTO> orcamentos = orcamentoService.listarPorOrdemServico(5001L);

        assertEquals(1, orcamentos.size());

        assertEquals(7201L, orcamentos.get(0).id());
    }

    @Test
    void deveImpedirListagemUsandoOrdemDeOutraEmpresa() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> orcamentoService.listarPorOrdemServico(6001L));

        assertEquals("Ordem de serviço com ID 6001 não encontrada", exception.getMessage());
    }

    @Test
    void deveImpedirCriacaoDeOrcamentoParaOrdemDeOutraEmpresa() {

        OrcamentoRequestDTO dto = mock(OrcamentoRequestDTO.class);

        when(dto.observacao()).thenReturn("Tentativa entre empresas");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> orcamentoService.criar(6001L, dto));

        assertEquals("Ordem de serviço com ID 6001 não encontrada", exception.getMessage());

        Integer quantidade = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM orcamento
                WHERE empresa_id = 8001
                """, Integer.class);

        assertEquals(1, quantidade);
    }

    @Test
    void deveCriarOrcamentoNaEmpresaDoUsuarioAutenticado() {

        OrcamentoRequestDTO dto = mock(OrcamentoRequestDTO.class);

        when(dto.observacao()).thenReturn("Novo orçamento da Empresa A");

        OrcamentoResponseDTO resultado = orcamentoService.criar(5001L, dto);

        assertNotNull(resultado.id());

        assertEquals(StatusOrcamento.RASCUNHO, resultado.status());

        Long empresaId = jdbcTemplate.queryForObject("""
                SELECT empresa_id
                FROM orcamento
                WHERE id = ?
                """, Long.class, resultado.id());

        assertEquals(8001L, empresaId);

        Long ordemServicoId = jdbcTemplate.queryForObject("""
                SELECT ordem_servico_id
                FROM orcamento
                WHERE id = ?
                """, Long.class, resultado.id());

        assertEquals(5001L, ordemServicoId);
    }

    @Test
    void deveImpedirAdicionarServicoDeOutraEmpresa() {

        OrcamentoItemRequestDTO dto = mock(OrcamentoItemRequestDTO.class);

        when(dto.servicoId()).thenReturn(7002L);

        when(dto.quantidade()).thenReturn(1);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> orcamentoService.adicionarItem(7201L, dto));

        assertEquals("Serviço com ID 7002 não encontrado", exception.getMessage());
    }

    @Test
    void deveImpedirAdicionarProdutoDeOutraEmpresa() {

        OrcamentoProdutoItemRequestDTO dto = mock(OrcamentoProdutoItemRequestDTO.class);

        when(dto.produtoId()).thenReturn(7102L);

        when(dto.quantidade()).thenReturn(1);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> orcamentoService.adicionarProduto(7201L, dto));

        assertEquals("Produto com ID 7102 não encontrado", exception.getMessage());
    }

    @Test
    void deveAdicionarServicoDaMesmaEmpresa() {

        OrcamentoItemRequestDTO dto = mock(OrcamentoItemRequestDTO.class);

        when(dto.servicoId()).thenReturn(7001L);

        when(dto.quantidade()).thenReturn(2);

        when(dto.valorUnitario()).thenReturn(null);

        OrcamentoItemResponseDTO resultado = orcamentoService.adicionarItem(7201L, dto);

        assertNotNull(resultado.id());

        assertEquals(7001L, resultado.servicoId());

        assertEquals(2, resultado.quantidade());

        assertEquals(0, new BigDecimal("300.00").compareTo(resultado.subtotal()));

        BigDecimal total = jdbcTemplate.queryForObject("""
                SELECT valor_total
                FROM orcamento
                WHERE id = 7201
                """, BigDecimal.class);

        assertEquals(0, new BigDecimal("300.00").compareTo(total));
    }

    @Test
    void deveAdicionarProdutoDaMesmaEmpresa() {

        OrcamentoProdutoItemRequestDTO dto = mock(OrcamentoProdutoItemRequestDTO.class);

        when(dto.produtoId()).thenReturn(7101L);

        when(dto.quantidade()).thenReturn(2);

        when(dto.valorUnitario()).thenReturn(null);

        OrcamentoItemResponseDTO resultado = orcamentoService.adicionarProduto(7201L, dto);

        assertNotNull(resultado.id());

        assertEquals(7101L, resultado.produtoId());

        assertEquals(2, resultado.quantidade());

        assertEquals(0, new BigDecimal("160.00").compareTo(resultado.subtotal()));
    }

    @Test
    void deveImpedirAlteracaoDeStatusDeOrcamentoDeOutraEmpresa() {

        AtualizarStatusOrcamentoRequestDTO dto = mock(AtualizarStatusOrcamentoRequestDTO.class);

        when(dto.status()).thenReturn(StatusOrcamento.CANCELADO);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> orcamentoService.atualizarStatus(7202L, dto));

        assertEquals("Orçamento com ID 7202 não encontrado", exception.getMessage());

        String status = jdbcTemplate.queryForObject("""
                SELECT status
                FROM orcamento
                WHERE id = 7202
                """, String.class);

        assertEquals("RASCUNHO", status);
    }

    @Test
    void deveImpedirAcessoAoHistoricoDeOrcamentoDeOutraEmpresa() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> orcamentoService.listarHistorico(7202L));

        assertEquals("Orçamento com ID 7202 não encontrado", exception.getMessage());
    }

    @Test
    void devePermitirEmpresaBVisualizarSeuProprioOrcamento() {

        autenticar("admin-b@teste.com", "ADMIN");

        OrcamentoResponseDTO resultado = orcamentoService.buscarPorId(7202L);

        assertEquals(7202L, resultado.id());

        assertEquals(6001L, resultado.ordemServicoId());
    }

    @Test
    void deveListarSomenteOrcamentosDaEmpresaAutenticada() {

        PageResponseDTO<OrcamentoResponseDTO> resultado = orcamentoService.listar(null, null, null, 0, 20);

        assertEquals(1, resultado.totalElements());

        assertEquals(7201L, resultado.content().get(0).id());

        assertTrue(resultado.content().stream().noneMatch(o -> o.id().equals(7202L)));
    }

    @Test
    void deveFiltrarOrcamentosPorStatus() {

        assertEquals(1, orcamentoService.listar(StatusOrcamento.RASCUNHO, null, null, 0, 20).totalElements());

        assertEquals(0, orcamentoService.listar(StatusOrcamento.APROVADO, null, null, 0, 20).totalElements());
    }

    @Test
    void devePaginarOrcamentosDaEmpresaAutenticada() {

        OrcamentoRequestDTO dto = mock(OrcamentoRequestDTO.class);

        when(dto.observacao()).thenReturn("Segundo orçamento da Empresa A");

        orcamentoService.criar(5001L, dto);

        PageResponseDTO<OrcamentoResponseDTO> primeiraPagina = orcamentoService.listar(null, null, null, 0, 1);

        assertEquals(1, primeiraPagina.content().size());

        assertEquals(2, primeiraPagina.totalElements());

        assertEquals(2, primeiraPagina.totalPages());
    }

    private void autenticar(String email, String role) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }
}