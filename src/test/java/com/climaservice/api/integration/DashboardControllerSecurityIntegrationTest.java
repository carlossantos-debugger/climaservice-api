package com.climaservice.api.integration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;

import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerSecurityIntegrationTest extends AbstractIntegrationTest {

    private static final String JWT_SECRET_TEST = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {

        jdbcTemplate.execute("""
                TRUNCATE TABLE empresa
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
                    (9002, true, CURRENT_TIMESTAMP, 'tecnico@teste.com', 'Técnico Teste', 'TECNICO', 'hash', 8001)
                """);
    }

    private String gerarToken(String email, String role) {

        SecretKey chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_TEST));

        Instant agora = Instant.now();

        return Jwts.builder().subject(email).claim("role", role).issuedAt(Date.from(agora)).expiration(Date.from(agora.plusSeconds(3600))).signWith(chave).compact();
    }

    @Test
    void deveRetornar401QuandoNaoEnviarTokenNoResumo() throws Exception {

        mockMvc.perform(get("/dashboard/resumo")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401QuandoNaoEnviarTokenNoFinanceiro() throws Exception {

        mockMvc.perform(get("/dashboard/financeiro")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401QuandoNaoEnviarTokenNoOperacional() throws Exception {

        mockMvc.perform(get("/dashboard/operacional")).andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirQualquerPerfilAutenticadoConsultarResumo() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(get("/dashboard/resumo").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void devePermitirQualquerPerfilAutenticadoConsultarFinanceiro() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(get("/dashboard/financeiro").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void devePermitirQualquerPerfilAutenticadoConsultarOperacional() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(get("/dashboard/operacional").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }
}
