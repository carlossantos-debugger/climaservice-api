package com.climaservice.api.integration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;

import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerSecurityIntegrationTest extends AbstractIntegrationTest {

    private static final String JWT_SECRET_TEST = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    @Autowired
    private MockMvc mockMvc;

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
                VALUES (8001, 'ClimaService Teste', NULL, true, CURRENT_TIMESTAMP)
                """);

        jdbcTemplate.update("""
                INSERT INTO usuario (id, ativo, data_criacao, email, nome, role, senha_hash, empresa_id)
                VALUES
                    (9001, true, CURRENT_TIMESTAMP, 'admin@teste.com', 'Administrador Teste', 'ADMIN', 'hash', 8001),
                    (9002, true, CURRENT_TIMESTAMP, 'atendente@teste.com', 'Atendente Teste', 'ATENDENTE', 'hash', 8001),
                    (9003, true, CURRENT_TIMESTAMP, 'tecnico@teste.com', 'Técnico Teste', 'TECNICO', 'hash', 8001)
                """);
    }

    private String gerarToken(String email, String role) {

        SecretKey chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_TEST));

        Instant agora = Instant.now();

        return Jwts.builder().subject(email).claim("role", role).issuedAt(Date.from(agora)).expiration(Date.from(agora.plusSeconds(3600))).signWith(chave).compact();
    }

    @Test
    void deveRetornar401QuandoNaoEnviarToken() throws Exception {

        mockMvc.perform(get("/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403QuandoAtendenteTentarListarUsuarios() throws Exception {

        String token = gerarToken("atendente@teste.com", "ATENDENTE");

        mockMvc.perform(get("/usuarios").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar403QuandoTecnicoTentarCadastrarUsuario() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(post("/usuarios").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "nome": "Novo Usuário", "email": "novo@teste.com", "senha": "senhaSegura123", "role": "ATENDENTE" }
                """)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminListarUsuarios() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(get("/usuarios").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void devePermitirAdminCadastrarUsuario() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(post("/usuarios").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "nome": "Novo Usuário", "email": "novo@teste.com", "senha": "senhaSegura123", "role": "ATENDENTE" }
                """)).andExpect(status().isCreated());
    }

    @Test
    void devePermitirAdminInativarUsuario() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(patch("/usuarios/{id}/inativar", 9003L).header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void deveRetornar403QuandoAtendenteTentarInativarUsuario() throws Exception {

        String token = gerarToken("atendente@teste.com", "ATENDENTE");

        mockMvc.perform(patch("/usuarios/{id}/inativar", 9003L).header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }
}
