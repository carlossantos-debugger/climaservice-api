package com.climaservice.api.integration;

import com.climaservice.api.dto.EquipamentoRequestDTO;
import com.climaservice.api.dto.EquipamentoResponseDTO;
import com.climaservice.api.dto.PageResponseDTO;
import com.climaservice.api.entity.StatusEquipamento;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.service.EquipamentoService;

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
class EquipamentoMultiTenancyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EquipamentoService equipamentoService;

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
                    ),
                    (
                        3002,
                        9000,
                        'Quarto Empresa A',
                        'Daikin',
                        'EcoSwing',
                        'SERIE-A2',
                        1001,
                        'INATIVO',
                        8001
                    )
                """);

        /*
         * Por padrão cada teste começa como
         * ADMIN da Empresa A.
         */
        autenticar("admin-a@teste.com", "ADMIN");
    }

    @AfterEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveListarSomenteEquipamentosDaEmpresaAutenticada() {

        // Act
        PageResponseDTO<EquipamentoResponseDTO> resultado = equipamentoService.listar(null, null, null, null, 0, 20);

        // Assert
        assertEquals(2, resultado.totalElements());

        List<EquipamentoResponseDTO> equipamentos = resultado.content();

        assertTrue(equipamentos.stream().anyMatch(e -> e.id().equals(3001L)));

        assertTrue(equipamentos.stream().noneMatch(e -> e.id().equals(4001L)));
    }

    @Test
    void devePaginarEquipamentosDaEmpresaAutenticada() {

        PageResponseDTO<EquipamentoResponseDTO> primeiraPagina = equipamentoService.listar(null, null, null, null, 0, 1);

        assertEquals(1, primeiraPagina.content().size());

        assertEquals(2, primeiraPagina.totalElements());

        assertEquals(2, primeiraPagina.totalPages());
    }

    @Test
    void deveFiltrarEquipamentosPorStatus() {

        PageResponseDTO<EquipamentoResponseDTO> resultado = equipamentoService.listar(null, StatusEquipamento.INATIVO, null, null, 0, 20);

        assertEquals(1, resultado.totalElements());

        assertEquals(3002L, resultado.content().get(0).id());
    }

    @Test
    void deveFiltrarEquipamentosPorMarcaEModelo() {

        PageResponseDTO<EquipamentoResponseDTO> porMarca = equipamentoService.listar(null, null, "daikin", null, 0, 20);

        assertEquals(1, porMarca.totalElements());

        assertEquals(3002L, porMarca.content().get(0).id());

        PageResponseDTO<EquipamentoResponseDTO> porModelo = equipamentoService.listar(null, null, null, "Dual Inverter", 0, 20);

        assertEquals(1, porModelo.totalElements());

        assertEquals(3001L, porModelo.content().get(0).id());
    }

    @Test
    void deveFiltrarEquipamentosPorCliente() {

        PageResponseDTO<EquipamentoResponseDTO> resultado = equipamentoService.listar(1001L, null, null, null, 0, 20);

        assertEquals(2, resultado.totalElements());
    }

    @Test
    void devePermitirBuscarEquipamentoDaPropriaEmpresa() {

        // Act
        Optional<EquipamentoResponseDTO> resultado = equipamentoService.buscarPorId(3001L);

        // Assert
        assertTrue(resultado.isPresent());

        assertEquals(3001L, resultado.get().id());

        assertEquals("LG", resultado.get().marca());
    }

    @Test
    void deveOcultarEquipamentoDeOutraEmpresa() {

        /*
         * Primeiro provamos que o equipamento
         * realmente existe no banco.
         */
        Integer quantidadeNoBanco = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM equipamento
                WHERE id = 4001
                """, Integer.class);

        assertEquals(1, quantidadeNoBanco);

        /*
         * Usuário da Empresa A tentando acessar
         * equipamento da Empresa B.
         */
        Optional<EquipamentoResponseDTO> resultado = equipamentoService.buscarPorId(4001L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveListarEquipamentosDaEmpresaBQuandoUsuarioBEstiverAutenticado() {

        // Arrange
        autenticar("admin-b@teste.com", "ADMIN");

        // Act
        PageResponseDTO<EquipamentoResponseDTO> resultado = equipamentoService.listar(null, null, null, null, 0, 20);

        // Assert
        assertEquals(1, resultado.totalElements());

        assertEquals(4001L, resultado.content().get(0).id());

        assertEquals("Samsung", resultado.content().get(0).marca());

        assertTrue(resultado.content().stream().noneMatch(e -> e.id().equals(3001L)));
    }

    @Test
    void deveImpedirCadastroDeEquipamentoComClienteDeOutraEmpresa() {

        // Arrange
        EquipamentoRequestDTO dto = mock(EquipamentoRequestDTO.class);

        /*
         * Cliente 2001 pertence à Empresa B,
         * enquanto estamos autenticados na Empresa A.
         */
        when(dto.clienteId()).thenReturn(2001L);

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> equipamentoService.salvar(dto));

        assertEquals("Cliente não encontrado", exception.getMessage());

        Integer quantidade = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM equipamento
                WHERE empresa_id = 8001
                """, Integer.class);

        /*
         * Continuam existindo apenas os equipamentos
         * originais da Empresa A.
         */
        assertEquals(2, quantidade);
    }

    @Test
    void devePermitirCadastrarEquipamentoParaClienteDaPropriaEmpresa() {

        // Arrange
        EquipamentoRequestDTO dto = mock(EquipamentoRequestDTO.class);

        when(dto.clienteId()).thenReturn(1001L);

        when(dto.marca()).thenReturn("Daikin");

        when(dto.modelo()).thenReturn("EcoSwing");

        when(dto.capacidadeBtu()).thenReturn(9000);

        when(dto.numeroSerie()).thenReturn("NOVO-A-001");

        when(dto.localInstalacao()).thenReturn("Quarto");

        // Act
        EquipamentoResponseDTO resposta = equipamentoService.salvar(dto);

        // Assert
        assertNotNull(resposta);

        assertNotNull(resposta.id());

        Long empresaId = jdbcTemplate.queryForObject("""
                SELECT empresa_id
                FROM equipamento
                WHERE numero_serie = 'NOVO-A-001'
                """, Long.class);

        Long clienteId = jdbcTemplate.queryForObject("""
                SELECT cliente_id
                FROM equipamento
                WHERE numero_serie = 'NOVO-A-001'
                """, Long.class);

        assertEquals(8001L, empresaId);

        assertEquals(1001L, clienteId);
    }

    @Test
    void deveImpedirInativacaoDeEquipamentoDeOutraEmpresa() {

        /*
         * Estamos autenticados na Empresa A,
         * mas equipamento 4001 pertence à Empresa B.
         */
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> equipamentoService.inativar(4001L));

        assertEquals("Equipamento com ID 4001 não encontrado", exception.getMessage());

        String status = jdbcTemplate.queryForObject("""
                SELECT status
                FROM equipamento
                WHERE id = 4001
                """, String.class);

        /*
         * Garante que o equipamento da outra empresa
         * não sofreu alteração.
         */
        assertEquals("ATIVO", status);
    }

    @Test
    void deveImpedirListagemPorClienteDeOutraEmpresa() {

        /*
         * Cliente 2001 pertence à Empresa B.
         */
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> equipamentoService.listarPorCliente(2001L));

        assertEquals("Cliente não encontrado", exception.getMessage());
    }

    private void autenticar(String email, String role) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }
}