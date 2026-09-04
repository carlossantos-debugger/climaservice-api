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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlanoManutencaoPreventivaControllerSecurityIntegrationTest extends AbstractIntegrationTest {

    private static final String JWT_SECRET_TEST = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {

        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    plano_manutencao_preventiva_execucao,
                    plano_manutencao_preventiva,
                    agendamento_historico,
                    agendamento,
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

        jdbcTemplate.update("""
                INSERT INTO cliente (id, nome, cpf_cnpj, telefone, email, empresa_id)
                VALUES (1001, 'Cliente Teste', '12345678901', '47999999999', 'cliente@teste.com', 8001)
                """);

        jdbcTemplate.update("""
                INSERT INTO equipamento (id, capacidade_btu, local_instalacao, marca, modelo, numero_serie, cliente_id, status, empresa_id)
                VALUES (2001, 12000, 'Sala', 'LG', 'Dual Inverter', 'SERIE-001', 1001, 'ATIVO', 8001)
                """);

        jdbcTemplate.update("""
                INSERT INTO plano_manutencao_preventiva (id, intervalo_meses, proxima_execucao, ultima_execucao, ativo, observacao, data_criacao, equipamento_id, tecnico_padrao_id, empresa_id)
                VALUES (6001, 6, CURRENT_DATE, NULL, true, 'Plano teste', CURRENT_TIMESTAMP, 2001, 9003, 8001)
                """);
    }

    private String gerarToken(String email, String role) {

        SecretKey chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_TEST));

        Instant agora = Instant.now();

        return Jwts.builder().subject(email).claim("role", role).issuedAt(Date.from(agora)).expiration(Date.from(agora.plusSeconds(3600))).signWith(chave).compact();
    }

    @Test
    void deveRetornar401QuandoNaoEnviarToken() throws Exception {

        mockMvc.perform(post("/planos-manutencao-preventiva").contentType(MediaType.APPLICATION_JSON).content("""
                { "equipamentoId": 2001, "intervaloMeses": 6 }
                """)).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403QuandoTecnicoTentarCriarPlano() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(post("/planos-manutencao-preventiva").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "equipamentoId": 2001, "intervaloMeses": 6 }
                """)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminCriarPlano() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(post("/planos-manutencao-preventiva").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "equipamentoId": 2001, "intervaloMeses": 6 }
                """)).andExpect(status().isCreated());
    }

    @Test
    void devePermitirAtendenteCriarPlano() throws Exception {

        String token = gerarToken("atendente@teste.com", "ATENDENTE");

        mockMvc.perform(post("/planos-manutencao-preventiva").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "equipamentoId": 2001, "intervaloMeses": 6 }
                """)).andExpect(status().isCreated());
    }

    @Test
    void deveRetornar403QuandoTecnicoTentarAtualizarPlano() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(put("/planos-manutencao-preventiva/{id}", 6001L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "intervaloMeses": 12, "proximaExecucao": "2027-01-01" }
                """)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminAtualizarPlano() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(put("/planos-manutencao-preventiva/{id}", 6001L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "intervaloMeses": 12, "proximaExecucao": "2027-01-01" }
                """)).andExpect(status().isOk());
    }

    @Test
    void deveRetornar403QuandoTecnicoTentarInativarPlano() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(patch("/planos-manutencao-preventiva/{id}/inativar", 6001L).header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminInativarPlano() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(patch("/planos-manutencao-preventiva/{id}/inativar", 6001L).header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void deveRetornar403QuandoTecnicoTentarGerarOrdemServico() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(post("/planos-manutencao-preventiva/{id}/gerar-ordem-servico", 6001L).header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminGerarOrdemServico() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(post("/planos-manutencao-preventiva/{id}/gerar-ordem-servico", 6001L).header("Authorization", "Bearer " + token)).andExpect(status().isCreated());
    }

    @Test
    void devePermitirQualquerPerfilAutenticadoConsultarPlanos() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(get("/planos-manutencao-preventiva").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }
}
