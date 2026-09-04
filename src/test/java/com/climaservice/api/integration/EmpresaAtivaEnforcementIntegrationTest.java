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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;

import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Garante que uma empresa inativa não consegue operar: nem via login
 * (AuthService), nem via um token já emitido antes da desativação
 * (JwtAuthFilter) — já que o JWT é stateless e não pode ser revogado
 * de outra forma antes de expirar.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EmpresaAtivaEnforcementIntegrationTest extends AbstractIntegrationTest {

    private static final String JWT_SECRET_TEST = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    private static final String SENHA_TEXTO_PURO = "senhaSegura123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararBanco() {

        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    usuario,
                    empresa
                RESTART IDENTITY CASCADE
                """);

        String senhaHash = passwordEncoder.encode(SENHA_TEXTO_PURO);

        jdbcTemplate.update("""
                INSERT INTO empresa (id, nome, cpf_cnpj, ativo, data_criacao)
                VALUES (8001, 'Empresa Ativa', NULL, true, CURRENT_TIMESTAMP),
                       (8002, 'Empresa Inativa', NULL, false, CURRENT_TIMESTAMP)
                """);

        jdbcTemplate.update("""
                INSERT INTO usuario (id, ativo, data_criacao, email, nome, role, senha_hash, empresa_id)
                VALUES
                    (9001, true, CURRENT_TIMESTAMP, 'admin-ativa@teste.com', 'Admin Empresa Ativa', 'ADMIN', ?, 8001),
                    (9002, true, CURRENT_TIMESTAMP, 'admin-inativa@teste.com', 'Admin Empresa Inativa', 'ADMIN', ?, 8002)
                """, senhaHash, senhaHash);
    }

    private String gerarToken(String email, String role) {

        SecretKey chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_TEST));

        Instant agora = Instant.now();

        return Jwts.builder().subject(email).claim("role", role).issuedAt(Date.from(agora)).expiration(Date.from(agora.plusSeconds(3600))).signWith(chave).compact();
    }

    @Test
    void devePermitirLoginParaUsuarioDeEmpresaAtiva() throws Exception {

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                { "email": "admin-ativa@teste.com", "senha": "%s" }
                """.formatted(SENHA_TEXTO_PURO))).andExpect(status().isOk());
    }

    @Test
    void deveImpedirLoginParaUsuarioDeEmpresaInativa() throws Exception {

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                { "email": "admin-inativa@teste.com", "senha": "%s" }
                """.formatted(SENHA_TEXTO_PURO))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Empresa inativa"));
    }

    @Test
    void tokenValidoDeUsuarioDeEmpresaAtivaContinuaFuncionando() throws Exception {

        String token = gerarToken("admin-ativa@teste.com", "ADMIN");

        mockMvc.perform(get("/clientes").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void deveRejeitarTokenDeUsuarioDeEmpresaInativaMesmoJaEmitido() throws Exception {

        // O token em si é válido (mesma assinatura/expiração de um
        // token real) — só a empresa do usuário está inativa.
        String token = gerarToken("admin-inativa@teste.com", "ADMIN");

        mockMvc.perform(get("/clientes").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }
}
