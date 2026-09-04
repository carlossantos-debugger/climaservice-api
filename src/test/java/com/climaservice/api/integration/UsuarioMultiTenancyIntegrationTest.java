package com.climaservice.api.integration;

import com.climaservice.api.dto.UsuarioResponseDTO;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.service.UsuarioService;

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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UsuarioMultiTenancyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {

        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    usuario,
                    empresa
                RESTART IDENTITY CASCADE
                """);

        jdbcTemplate.update("""
                INSERT INTO empresa (id, nome, cpf_cnpj, ativo, data_criacao)
                VALUES (8001, 'Empresa A', NULL, true, CURRENT_TIMESTAMP),
                       (8002, 'Empresa B', NULL, true, CURRENT_TIMESTAMP)
                """);

        // Empresa A tem dois ADMINs ativos, um TECNICO e um ATENDENTE.
        jdbcTemplate.update("""
                INSERT INTO usuario (id, ativo, data_criacao, email, nome, role, senha_hash, empresa_id)
                VALUES
                    (9001, true, CURRENT_TIMESTAMP, 'admin-a@teste.com', 'Administrador A', 'ADMIN', 'hash', 8001),
                    (9002, true, CURRENT_TIMESTAMP, 'tecnico-a@teste.com', 'Técnico A', 'TECNICO', 'hash', 8001),
                    (9003, true, CURRENT_TIMESTAMP, 'admin-a-2@teste.com', 'Administrador A2', 'ADMIN', 'hash', 8001),
                    (9101, true, CURRENT_TIMESTAMP, 'admin-b@teste.com', 'Administrador B', 'ADMIN', 'hash', 8002)
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
    void deveListarSomenteUsuariosDaEmpresaAutenticada() {

        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos();

        assertEquals(3, usuarios.size());

        assertTrue(usuarios.stream().noneMatch(u -> u.id().equals(9101L)));
    }

    @Test
    void deveOcultarUsuarioDeOutraEmpresa() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> usuarioService.buscarPorId(9101L));

        assertEquals("Usuário com ID 9101 não encontrado", exception.getMessage());
    }

    @Test
    void deveImpedirInativarUsuarioDeOutraEmpresa() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> usuarioService.inativar(9101L));

        assertEquals("Usuário com ID 9101 não encontrado", exception.getMessage());

        Boolean ativo = jdbcTemplate.queryForObject("SELECT ativo FROM usuario WHERE id = 9101", Boolean.class);

        assertTrue(ativo);
    }

    @Test
    void deveImpedirAtivarUsuarioDeOutraEmpresa() {

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.ativar(9101L));
    }

    @Test
    void deveInativarUsuarioNaoAdmin() {

        UsuarioResponseDTO response = usuarioService.inativar(9002L);

        assertFalse(response.ativo());
    }

    @Test
    void deveInativarAdminQuandoHouverOutroAdminAtivo() {

        UsuarioResponseDTO response = usuarioService.inativar(9003L);

        assertFalse(response.ativo());
    }

    @Test
    void deveImpedirInativarUltimoAdminAtivoDaEmpresa() {

        // Reduz a empresa a um único ADMIN ativo (9001).
        usuarioService.inativar(9003L);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> usuarioService.inativar(9001L));

        assertEquals("A empresa não pode ficar sem nenhum administrador ativo", exception.getMessage());

        Boolean ativo = jdbcTemplate.queryForObject("SELECT ativo FROM usuario WHERE id = 9001", Boolean.class);

        assertTrue(ativo);
    }

    @Test
    void naoDeveImpedirInativarUltimoAdminSeEmpresaBTiverOSeuProprio() {

        // Inativar o único admin da Empresa B não deve ser afetado
        // pelo estado de admins da Empresa A.
        autenticar("admin-b@teste.com", "ADMIN");

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> usuarioService.inativar(9101L));

        assertEquals("A empresa não pode ficar sem nenhum administrador ativo", exception.getMessage());
    }

    @Test
    void deveMudarIsolamentoQuandoUsuarioDaEmpresaBForAutenticado() {

        autenticar("admin-b@teste.com", "ADMIN");

        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos();

        assertEquals(1, usuarios.size());

        assertEquals(9101L, usuarios.get(0).id());
    }
}
