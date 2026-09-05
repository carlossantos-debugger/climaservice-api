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
class NotaFiscalServicoControllerSecurityIntegrationTest extends AbstractIntegrationTest {

    private static final String JWT_SECRET_TEST = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {

        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    nota_fiscal_servico,
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
                INSERT INTO empresa (id, nome, cpf_cnpj, ativo, data_criacao, logradouro, numero, bairro, cidade, uf, cep, regime_tributario, codigo_servico_padrao, aliquota_iss_padrao)
                VALUES (8001, 'ClimaService Teste', NULL, true, CURRENT_TIMESTAMP, 'Rua Teste', '1', 'Centro', 'Brusque', 'SC', '88350000', 'SIMPLES_NACIONAL', '01.07', 5.00)
                """);

        jdbcTemplate.update("""
                INSERT INTO usuario (id, ativo, data_criacao, email, nome, role, senha_hash, empresa_id)
                VALUES
                    (9001, true, CURRENT_TIMESTAMP, 'admin@teste.com', 'Administrador Teste', 'ADMIN', 'hash', 8001),
                    (9002, true, CURRENT_TIMESTAMP, 'atendente@teste.com', 'Atendente Teste', 'ATENDENTE', 'hash', 8001),
                    (9003, true, CURRENT_TIMESTAMP, 'tecnico@teste.com', 'Técnico Teste', 'TECNICO', 'hash', 8001)
                """);

        jdbcTemplate.update("""
                INSERT INTO cliente (id, nome, cpf_cnpj, telefone, email, empresa_id, logradouro, numero, bairro, cidade, uf, cep)
                VALUES (1001, 'Cliente Teste', '12345678901', '47999999999', 'cliente@teste.com', 8001, 'Rua Cliente', '10', 'Centro', 'Brusque', 'SC', '88350000')
                """);

        jdbcTemplate.update("""
                INSERT INTO equipamento (id, capacidade_btu, local_instalacao, marca, modelo, numero_serie, cliente_id, status, empresa_id)
                VALUES (2001, 12000, 'Sala', 'LG', 'Dual Inverter', 'SERIE-001', 1001, 'ATIVO', 8001)
                """);

        jdbcTemplate.update("""
                INSERT INTO ordem_servico (id, data_abertura, descricao_problema, diagnostico, status, cliente_id, equipamento_id, empresa_id)
                VALUES (3001, CURRENT_TIMESTAMP, 'Equipamento não está resfriando', NULL, 'CONCLUIDA', 1001, 2001, 8001),
                       (3002, CURRENT_TIMESTAMP, 'Equipamento com ruído', NULL, 'CONCLUIDA', 1001, 2001, 8001)
                """);

        jdbcTemplate.update("""
                INSERT INTO orcamento (id, ordem_servico_id, status, valor_total, data_criacao, empresa_id)
                VALUES (4001, 3001, 'APROVADO', 1000.00, CURRENT_TIMESTAMP, 8001),
                       (4002, 3002, 'APROVADO', 800.00, CURRENT_TIMESTAMP, 8001)
                """);

        jdbcTemplate.update("""
                INSERT INTO nota_fiscal_servico (id, status, ambiente, discriminacao_servico, codigo_servico, aliquota_iss, valor_servico, valor_iss, data_criacao, ordem_servico_id, orcamento_id, empresa_id)
                VALUES (5001, 'RASCUNHO', 'HOMOLOGACAO', 'Manutenção teste', '01.07', 5.00, 1000.00, 50.00, CURRENT_TIMESTAMP, 3001, 4001, 8001)
                """);
    }

    private String gerarToken(String email, String role) {

        SecretKey chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_TEST));

        Instant agora = Instant.now();

        return Jwts.builder().subject(email).claim("role", role).issuedAt(Date.from(agora)).expiration(Date.from(agora.plusSeconds(3600))).signWith(chave).compact();
    }

    @Test
    void deveRetornar401QuandoNaoEnviarToken() throws Exception {

        mockMvc.perform(post("/ordens-servico/{id}/nota-fiscal-servico", 3002L).contentType(MediaType.APPLICATION_JSON).content("""
                { "discriminacaoServico": "Serviço prestado" }
                """)).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403QuandoTecnicoTentarCriarNota() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(post("/ordens-servico/{id}/nota-fiscal-servico", 3002L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "discriminacaoServico": "Serviço prestado" }
                """)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminCriarNota() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(post("/ordens-servico/{id}/nota-fiscal-servico", 3002L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "discriminacaoServico": "Serviço prestado" }
                """)).andExpect(status().isCreated());
    }

    @Test
    void devePermitirAtendenteCriarNota() throws Exception {

        String token = gerarToken("atendente@teste.com", "ATENDENTE");

        mockMvc.perform(post("/ordens-servico/{id}/nota-fiscal-servico", 3002L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "discriminacaoServico": "Serviço prestado" }
                """)).andExpect(status().isCreated());
    }

    @Test
    void deveRetornar403QuandoTecnicoTentarAtualizarNota() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(put("/notas-fiscais-servico/{id}", 5001L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "discriminacaoServico": "Nova descrição" }
                """)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminAtualizarNota() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(put("/notas-fiscais-servico/{id}", 5001L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                { "discriminacaoServico": "Nova descrição" }
                """)).andExpect(status().isOk());
    }

    @Test
    void deveRetornar403QuandoTecnicoTentarGerarPayload() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(post("/notas-fiscais-servico/{id}/gerar-payload", 5001L).header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminGerarPayload() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(post("/notas-fiscais-servico/{id}/gerar-payload", 5001L).header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void deveRetornar400AoTentarEnviarPoisAindaNaoDisponivel() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(post("/notas-fiscais-servico/{id}/enviar", 5001L).header("Authorization", "Bearer " + token)).andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar403QuandoTecnicoTentarCancelar() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(patch("/notas-fiscais-servico/{id}/cancelar", 5001L).header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminCancelar() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(patch("/notas-fiscais-servico/{id}/cancelar", 5001L).header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void devePermitirQualquerPerfilAutenticadoConsultarNotas() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(get("/notas-fiscais-servico").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }
}
