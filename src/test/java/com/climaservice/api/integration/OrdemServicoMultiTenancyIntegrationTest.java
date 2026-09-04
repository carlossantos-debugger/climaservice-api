package com.climaservice.api.integration;

import com.climaservice.api.dto.AtualizarDiagnosticoRequestDTO;
import com.climaservice.api.dto.AtualizarStatusOrdemServicoRequestDTO;
import com.climaservice.api.dto.OrdemServicoRequestDTO;
import com.climaservice.api.dto.OrdemServicoResponseDTO;
import com.climaservice.api.dto.PageResponseDTO;
import com.climaservice.api.entity.StatusOrdemServico;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.service.OrdemServicoService;

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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class OrdemServicoMultiTenancyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrdemServicoService ordemServicoService;

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
                        'Cliente Empresa A',
                        '11111111111',
                        '47911111111',
                        'cliente-a@teste.com',
                        8001
                    ),
                    (
                        2001,
                        'Cliente Empresa B',
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
                        'Sala Empresa A',
                        'LG',
                        'Dual Inverter A',
                        'SERIE-A',
                        1001,
                        'ATIVO',
                        8001
                    ),
                    (
                        4001,
                        18000,
                        'Sala Empresa B',
                        'Samsung',
                        'WindFree B',
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
                        'Problema da Empresa A',
                        NULL,
                        'ABERTA',
                        1001,
                        3001,
                        8001
                    ),
                    (
                        6001,
                        CURRENT_TIMESTAMP,
                        'Problema da Empresa B',
                        NULL,
                        'ABERTA',
                        2001,
                        4001,
                        8002
                    ),
                    (
                        5002,
                        CURRENT_TIMESTAMP,
                        'Segundo problema da Empresa A',
                        NULL,
                        'EM_ANDAMENTO',
                        1001,
                        3001,
                        8001
                    )
                """);

        /*
         * Cada teste começa autenticado
         * como ADMIN da Empresa A.
         */
        autenticar("admin-a@teste.com", "ADMIN");
    }

    @AfterEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveListarSomenteOrdensDaEmpresaAutenticada() {

        // Act
        PageResponseDTO<OrdemServicoResponseDTO> resultado = ordemServicoService.listar(null, null, null, null, null, 0, 20);

        // Assert
        assertEquals(2, resultado.totalElements());

        List<OrdemServicoResponseDTO> ordens = resultado.content();

        assertTrue(ordens.stream().anyMatch(ordem -> ordem.id().equals(5001L) && "Problema da Empresa A".equals(ordem.descricaoProblema())));

        assertTrue(ordens.stream().noneMatch(ordem -> ordem.id().equals(6001L)));
    }

    @Test
    void devePaginarOrdensDaEmpresaAutenticada() {

        PageResponseDTO<OrdemServicoResponseDTO> primeiraPagina = ordemServicoService.listar(null, null, null, null, null, 0, 1);

        assertEquals(1, primeiraPagina.content().size());

        assertEquals(2, primeiraPagina.totalElements());

        assertEquals(2, primeiraPagina.totalPages());
    }

    @Test
    void deveFiltrarOrdensPorStatus() {

        PageResponseDTO<OrdemServicoResponseDTO> resultado = ordemServicoService.listar(StatusOrdemServico.EM_ANDAMENTO, null, null, null, null, 0, 20);

        assertEquals(1, resultado.totalElements());

        assertEquals(5002L, resultado.content().get(0).id());
    }

    @Test
    void deveFiltrarOrdensPorClienteEEquipamento() {

        PageResponseDTO<OrdemServicoResponseDTO> resultado = ordemServicoService.listar(null, 1001L, 3001L, null, null, 0, 20);

        assertEquals(2, resultado.totalElements());
    }

    @Test
    void devePermitirBuscarOrdemDaPropriaEmpresa() {

        // Act
        Optional<OrdemServicoResponseDTO> resultado = ordemServicoService.buscarPorId(5001L);

        // Assert
        assertTrue(resultado.isPresent());

        assertEquals(5001L, resultado.get().id());

        assertEquals(1001L, resultado.get().clienteId());

        assertEquals(3001L, resultado.get().equipamentoId());
    }

    @Test
    void deveOcultarOrdemDeOutraEmpresa() {

        /*
         * Confirmamos primeiro que a OS da Empresa B
         * realmente existe no PostgreSQL.
         */
        Integer quantidadeNoBanco = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM ordem_servico
                WHERE id = 6001
                """, Integer.class);

        assertEquals(1, quantidadeNoBanco);

        // Act
        Optional<OrdemServicoResponseDTO> resultado = ordemServicoService.buscarPorId(6001L);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveListarSomenteOrdensDaEmpresaBQuandoUsuarioBForAutenticado() {

        // Arrange
        autenticar("admin-b@teste.com", "ADMIN");

        // Act
        PageResponseDTO<OrdemServicoResponseDTO> resultado = ordemServicoService.listar(null, null, null, null, null, 0, 20);

        // Assert
        assertEquals(1, resultado.totalElements());

        assertEquals(6001L, resultado.content().get(0).id());

        assertTrue(resultado.content().stream().noneMatch(ordem -> ordem.id().equals(5001L)));
    }

    @Test
    void deveImpedirCriacaoComClienteDeOutraEmpresa() {

        // Arrange
        OrdemServicoRequestDTO dto = mock(OrdemServicoRequestDTO.class);

        /*
         * Cliente 2001 pertence à Empresa B,
         * mas estamos autenticados na Empresa A.
         */
        when(dto.clienteId()).thenReturn(2001L);

        when(dto.equipamentoId()).thenReturn(4001L);

        when(dto.descricaoProblema()).thenReturn("Tentativa entre empresas");

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.salvar(dto));

        assertEquals("Cliente com ID 2001 não encontrado", exception.getMessage());

        Integer quantidadeEmpresaA = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM ordem_servico
                WHERE empresa_id = 8001
                """, Integer.class);

        /*
         * Continuam existindo apenas as OS originais da Empresa A.
         */
        assertEquals(2, quantidadeEmpresaA);
    }

    @Test
    void devePermitirCriarOrdemComClienteEEquipamentoDaPropriaEmpresa() {

        // Arrange
        OrdemServicoRequestDTO dto = mock(OrdemServicoRequestDTO.class);

        when(dto.clienteId()).thenReturn(1001L);

        when(dto.equipamentoId()).thenReturn(3001L);

        when(dto.descricaoProblema()).thenReturn("Equipamento fazendo ruído");

        // Act
        OrdemServicoResponseDTO resposta = ordemServicoService.salvar(dto);

        // Assert
        assertNotNull(resposta);

        assertNotNull(resposta.id());

        assertEquals(StatusOrdemServico.ABERTA, resposta.status());

        Long empresaId = jdbcTemplate.queryForObject("""
                SELECT empresa_id
                FROM ordem_servico
                WHERE id = ?
                """, Long.class, resposta.id());

        Long clienteId = jdbcTemplate.queryForObject("""
                SELECT cliente_id
                FROM ordem_servico
                WHERE id = ?
                """, Long.class, resposta.id());

        Long equipamentoId = jdbcTemplate.queryForObject("""
                SELECT equipamento_id
                FROM ordem_servico
                WHERE id = ?
                """, Long.class, resposta.id());

        assertEquals(8001L, empresaId);

        assertEquals(1001L, clienteId);

        assertEquals(3001L, equipamentoId);

        /*
         * Também verifica que a criação gerou
         * o histórico inicial da OS.
         */
        Integer quantidadeHistorico = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM ordem_servico_historico
                WHERE ordem_servico_id = ?
                """, Integer.class, resposta.id());

        assertEquals(1, quantidadeHistorico);
    }

    @Test
    void deveImpedirAlteracaoDeStatusDeOrdemDeOutraEmpresa() {

        // Arrange
        AtualizarStatusOrdemServicoRequestDTO dto = mock(AtualizarStatusOrdemServicoRequestDTO.class);

        when(dto.status()).thenReturn(StatusOrdemServico.EM_ANDAMENTO);

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.atualizarStatus(6001L, dto));

        assertEquals("Ordem de serviço com ID 6001 não encontrada", exception.getMessage());

        String status = jdbcTemplate.queryForObject("""
                SELECT status
                FROM ordem_servico
                WHERE id = 6001
                """, String.class);

        /*
         * OS da Empresa B não pode ter sido alterada.
         */
        assertEquals("ABERTA", status);
    }

    @Test
    void deveImpedirAlteracaoDeDiagnosticoDeOrdemDeOutraEmpresa() {

        // Arrange
        AtualizarDiagnosticoRequestDTO dto = mock(AtualizarDiagnosticoRequestDTO.class);

        when(dto.diagnostico()).thenReturn("Tentativa de alteração");

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.atualizarDiagnostico(6001L, dto));

        assertEquals("Ordem de serviço com ID 6001 não encontrada", exception.getMessage());

        String diagnostico = jdbcTemplate.queryForObject("""
                SELECT COALESCE(diagnostico, '')
                FROM ordem_servico
                WHERE id = 6001
                """, String.class);

        assertEquals("", diagnostico);
    }

    @Test
    void deveImpedirAcessoAoHistoricoDeOrdemDeOutraEmpresa() {

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.listarHistorico(6001L));

        assertEquals("Ordem de serviço com ID 6001 não encontrada", exception.getMessage());
    }

    @Test
    void deveImpedirAcessoAoHistoricoDeDiagnosticoDeOutraEmpresa() {

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.listarHistoricoDiagnostico(6001L));

        assertEquals("Ordem de serviço com ID 6001 não encontrada", exception.getMessage());
    }

    private void autenticar(String email, String role) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }
}