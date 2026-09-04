package com.climaservice.api.integration;

import com.climaservice.api.dto.ClienteResponseDTO;
import com.climaservice.api.dto.PageResponseDTO;
import com.climaservice.api.service.ClienteService;

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

@SpringBootTest
class ClienteMultiTenancyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {

        /*
         * Limpamos os dados para que cada teste seja totalmente independente.
         */
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
         * Empresa A
         */
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
                    'Empresa A',
                    NULL,
                    true,
                    CURRENT_TIMESTAMP
                )
                """);

        /*
         * Empresa B
         */
        jdbcTemplate.update("""
                INSERT INTO empresa (
                    id,
                    nome,
                    cpf_cnpj,
                    ativo,
                    data_criacao
                )
                VALUES (
                    8002,
                    'Empresa B',
                    NULL,
                    true,
                    CURRENT_TIMESTAMP
                )
                """);

        /*
         * Administrador da Empresa A
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
                VALUES (
                    9001,
                    true,
                    CURRENT_TIMESTAMP,
                    'admin-a@teste.com',
                    'Administrador Empresa A',
                    'ADMIN',
                    'hash-teste',
                    8001
                )
                """);

        /*
         * Administrador da Empresa B
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
                VALUES (
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
         * Cliente da Empresa A
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
                VALUES (
                    1001,
                    'Cliente Empresa A',
                    '11111111111',
                    '47911111111',
                    'cliente-a@teste.com',
                    8001
                )
                """);

        /*
         * Cliente da Empresa B
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
                VALUES (
                    2001,
                    'Cliente Empresa B',
                    '22222222222',
                    '47922222222',
                    'cliente-b@teste.com',
                    8002
                )
                """);

        /*
         * Segundo cliente da Empresa A, usado
         * nos testes de filtro e paginação.
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
                VALUES (
                    1002,
                    'Outro Cliente A',
                    '33333333333',
                    '47933333333',
                    'outro-a@teste.com',
                    8001
                )
                """);

        /*
         * Por padrão, cada teste começa autenticado
         * como ADMIN da Empresa A.
         */
        autenticar("admin-a@teste.com", "ADMIN");
    }

    @AfterEach
    void limparSecurityContext() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void deveListarSomenteClientesDaEmpresaAutenticada() {

        // Act
        PageResponseDTO<ClienteResponseDTO> resultado = clienteService.listar(null, null, 0, 20);

        // Assert
        assertEquals(2, resultado.totalElements());

        List<ClienteResponseDTO> clientes = resultado.content();

        /*
         * Ordenados por nome ASC: "Cliente Empresa A" vem antes de "Outro Cliente A".
         */
        assertEquals(1001L, clientes.get(0).id());

        /*
         * Garante explicitamente que nenhum cliente
         * da Empresa B vazou para a Empresa A.
         */
        assertTrue(clientes.stream().noneMatch(c -> c.id().equals(2001L)));
    }

    @Test
    void devePaginarClientesDaEmpresaAutenticada() {

        PageResponseDTO<ClienteResponseDTO> primeiraPagina = clienteService.listar(null, null, 0, 1);

        assertEquals(1, primeiraPagina.content().size());

        assertEquals(2, primeiraPagina.totalElements());

        assertEquals(2, primeiraPagina.totalPages());

        assertTrue(primeiraPagina.first());

        assertFalse(primeiraPagina.last());

        PageResponseDTO<ClienteResponseDTO> segundaPagina = clienteService.listar(null, null, 1, 1);

        assertEquals(1, segundaPagina.content().size());

        assertTrue(segundaPagina.last());

        assertNotEquals(primeiraPagina.content().get(0).id(), segundaPagina.content().get(0).id());
    }

    @Test
    void deveFiltrarClientesPorNomeIgnorandoCaixa() {

        PageResponseDTO<ClienteResponseDTO> resultado = clienteService.listar("outro", null, 0, 20);

        assertEquals(1, resultado.totalElements());

        assertEquals(1002L, resultado.content().get(0).id());
    }

    @Test
    void deveFiltrarClientesPorCpfCnpjExato() {

        PageResponseDTO<ClienteResponseDTO> resultado = clienteService.listar(null, "11111111111", 0, 20);

        assertEquals(1, resultado.totalElements());

        assertEquals(1001L, resultado.content().get(0).id());
    }

    @Test
    void devePermitirBuscarClienteDaPropriaEmpresa() {

        // Act
        Optional<ClienteResponseDTO> resultado = clienteService.buscarPorId(1001L);

        // Assert
        assertTrue(resultado.isPresent());

        assertEquals(1001L, resultado.get().id());

        assertEquals("Cliente Empresa A", resultado.get().nome());
    }

    @Test
    void deveOcultarClienteDeOutraEmpresa() {

        /*
         * Primeiro provamos que o cliente realmente
         * existe no banco.
         */
        Integer quantidadeNoBanco = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM cliente
                WHERE id = 2001
                """, Integer.class);

        assertEquals(1, quantidadeNoBanco);

        /*
         * Usuário autenticado pertence à Empresa A.
         *
         * O cliente 2001 pertence à Empresa B.
         */
        Optional<ClienteResponseDTO> resultado = clienteService.buscarPorId(2001L);

        /*
         * Para a Empresa A, o registro deve ser
         * tratado como inexistente.
         */
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveMudarIsolamentoQuandoUsuarioDaEmpresaBForAutenticado() {

        // Arrange
        autenticar("admin-b@teste.com", "ADMIN");

        // Act
        PageResponseDTO<ClienteResponseDTO> resultado = clienteService.listar(null, null, 0, 20);

        // Assert
        assertEquals(1, resultado.totalElements());

        ClienteResponseDTO cliente = resultado.content().get(0);

        assertEquals(2001L, cliente.id());

        assertEquals("Cliente Empresa B", cliente.nome());

        /*
         * Cliente da Empresa A também deve ficar invisível.
         */
        assertTrue(resultado.content().stream().noneMatch(c -> c.id().equals(1001L)));
    }

    @Test
    void deveImpedirExclusaoDeClienteDeOutraEmpresa() {

        /*
         * Usuário da Empresa A tenta excluir
         * cliente da Empresa B.
         */
        clienteService.excluir(2001L);

        /*
         * O cliente precisa continuar existindo.
         */
        Integer quantidade = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM cliente
                WHERE id = 2001
                  AND empresa_id = 8002
                """, Integer.class);

        assertEquals(1, quantidade);
    }

    @Test
    void devePermitirExclusaoDeClienteDaPropriaEmpresa() {

        /*
         * Cliente 1001 pertence à Empresa A,
         * que é a empresa do usuário autenticado.
         */
        clienteService.excluir(1001L);

        Integer quantidade = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM cliente
                WHERE id = 1001
                """, Integer.class);

        assertEquals(0, quantidade);
    }

    /*
     * Helper responsável por simular
     * um usuário autenticado.
     */
    private void autenticar(String email, String role) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }
}