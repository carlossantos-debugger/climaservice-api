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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AgendamentoControllerSecurityIntegrationTest extends AbstractIntegrationTest {

    private static final String JWT_SECRET_TEST = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {

        jdbcTemplate.execute("""
                TRUNCATE TABLE
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
                INSERT INTO ordem_servico (id, data_abertura, descricao_problema, diagnostico, status, cliente_id, equipamento_id, empresa_id)
                VALUES (3001, CURRENT_TIMESTAMP, 'Equipamento não está resfriando', NULL, 'ABERTA', 1001, 2001, 8001)
                """);

        jdbcTemplate.update("""
                INSERT INTO agendamento (id, data_hora_inicio, data_hora_fim, status, observacao, data_criacao, ordem_servico_id, tecnico_id, empresa_id)
                VALUES (5001, '2026-09-01 09:00:00', '2026-09-01 10:00:00', 'AGENDADO', 'Agendamento teste', CURRENT_TIMESTAMP, 3001, 9003, 8001)
                """);
    }

    private String gerarToken(String email, String role) {

        SecretKey chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_TEST));

        Instant agora = Instant.now();

        return Jwts.builder().subject(email).claim("role", role).issuedAt(Date.from(agora)).expiration(Date.from(agora.plusSeconds(3600))).signWith(chave).compact();
    }

    @Test
    void deveRetornar401QuandoNaoEnviarToken() throws Exception {

        mockMvc.perform(post("/agendamentos").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "ordemServicoId": 3001,
                    "tecnicoId": 9003,
                    "dataHoraInicio": "2026-09-02T09:00:00",
                    "dataHoraFim": "2026-09-02T10:00:00"
                }
                """)).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403QuandoTecnicoTentarCriarAgendamento() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(post("/agendamentos").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "ordemServicoId": 3001,
                    "tecnicoId": 9003,
                    "dataHoraInicio": "2026-09-02T09:00:00",
                    "dataHoraFim": "2026-09-02T10:00:00"
                }
                """)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminCriarAgendamento() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(post("/agendamentos").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "ordemServicoId": 3001,
                    "tecnicoId": 9003,
                    "dataHoraInicio": "2026-09-02T09:00:00",
                    "dataHoraFim": "2026-09-02T10:00:00"
                }
                """)).andExpect(status().isCreated());
    }

    @Test
    void devePermitirAtendenteCriarAgendamento() throws Exception {

        String token = gerarToken("atendente@teste.com", "ATENDENTE");

        mockMvc.perform(post("/agendamentos").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "ordemServicoId": 3001,
                    "tecnicoId": 9003,
                    "dataHoraInicio": "2026-09-03T09:00:00",
                    "dataHoraFim": "2026-09-03T10:00:00"
                }
                """)).andExpect(status().isCreated());
    }

    @Test
    void deveRetornar403QuandoAtendenteTentarAtualizarStatus() throws Exception {

        String token = gerarToken("atendente@teste.com", "ATENDENTE");

        mockMvc.perform(patch("/agendamentos/{id}/status", 5001L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "status": "CONFIRMADO" }
                """)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirTecnicoAtualizarStatus() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(patch("/agendamentos/{id}/status", 5001L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "status": "CONFIRMADO" }
                """)).andExpect(status().isOk());

        String status = jdbcTemplate.queryForObject("SELECT status FROM agendamento WHERE id = 5001", String.class);

        assertEquals("CONFIRMADO", status);
    }

    @Test
    void deveRetornar403QuandoTecnicoTentarReagendar() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(patch("/agendamentos/{id}/reagendar", 5001L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "dataHoraInicio": "2026-09-05T09:00:00",
                    "dataHoraFim": "2026-09-05T10:00:00"
                }
                """)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminReagendar() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(patch("/agendamentos/{id}/reagendar", 5001L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "dataHoraInicio": "2026-09-05T09:00:00",
                    "dataHoraFim": "2026-09-05T10:00:00"
                }
                """)).andExpect(status().isOk());
    }

    @Test
    void devePermitirQualquerPerfilAutenticadoConsultarAgendamentos() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(get("/agendamentos").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }
}
