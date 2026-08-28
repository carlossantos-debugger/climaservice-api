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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PagamentoControllerSecurityIntegrationTest extends AbstractIntegrationTest {

    private static final String JWT_SECRET_TEST = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    @Autowired
    private MockMvc mockMvc;

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
                    'ClimaService Teste',
                    NULL,
                    true,
                    CURRENT_TIMESTAMP
                )
                """);

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
                        'admin@teste.com',
                        'Administrador Teste',
                        'ADMIN',
                        'hash-teste',
                        8001
                    ),
                    (
                        9002,
                        true,
                        CURRENT_TIMESTAMP,
                        'atendente@teste.com',
                        'Atendente Teste',
                        'ATENDENTE',
                        'hash-teste',
                        8001
                    ),
                    (
                        9003,
                        true,
                        CURRENT_TIMESTAMP,
                        'tecnico@teste.com',
                        'Técnico Teste',
                        'TECNICO',
                        'hash-teste',
                        8001
                    )
                """);

// Cliente
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
                    'Cliente Teste',
                    '12345678901',
                    '47999999999',
                    'cliente@teste.com',
                    8001
                )
                """);

        // Equipamento
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
        VALUES (
            2001,
            12000,
            'Sala',
            'LG',
            'Dual Inverter',
            'SERIE-001',
            1001,
            'ATIVO',
            8001
        )
        """);

        // Ordem de Serviço
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
        VALUES (
            3001,
            CURRENT_TIMESTAMP,
            'Equipamento não está resfriando',
            NULL,
            'ABERTA',
            1001,
            2001,
            8001
        )
        """);

        // Orçamento aprovado
        jdbcTemplate.update("""
                INSERT INTO orcamento (
                    id,
                    data_criacao,
                    data_envio,
                    data_resposta,
                    observacao,
                    status,
                    valor_total,
                    ordem_servico_id
                )
                VALUES (
                    4001,
                    CURRENT_TIMESTAMP,
                    NULL,
                    NULL,
                    'Orçamento de teste',
                    'APROVADO',
                    1000.00,
                    3001
                )
                """);
    }


    @Test
    void deveRetornar401QuandoNaoEnviarToken() throws Exception {

        mockMvc.perform(post("/orcamentos/{orcamentoId}/pagamentos", 4001L).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "valor": 100.00,
                    "formaPagamento": "PIX",
                    "observacao": "Pagamento teste"
                }
                """)).andExpect(status().isUnauthorized());
    }


    @Test
    void deveRetornar401QuandoTokenForInvalido() throws Exception {

        mockMvc.perform(post("/orcamentos/{orcamentoId}/pagamentos", 4001L).header("Authorization", "Bearer token-invalido").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "valor": 100.00,
                    "formaPagamento": "PIX",
                    "observacao": "Pagamento teste"
                }
                """)).andExpect(status().isUnauthorized());
    }


    @Test
    void deveRetornar403QuandoTecnicoTentarCriarPagamento() throws Exception {

        String token = gerarToken("tecnico@teste.com", "TECNICO");

        mockMvc.perform(post("/orcamentos/{orcamentoId}/pagamentos", 4001L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "valor": 100.00,
                    "formaPagamento": "PIX",
                    "observacao": "Pagamento teste"
                }
                """)).andExpect(status().isForbidden());
    }


    private String gerarToken(String email, String role) {

        SecretKey chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_TEST));

        Instant agora = Instant.now();

        return Jwts.builder().subject(email).claim("role", role).issuedAt(Date.from(agora)).expiration(Date.from(agora.plusSeconds(3600))).signWith(chave).compact();
    }

    @Test
    void devePermitirAdminCriarPagamento() throws Exception {

        String token = gerarToken("admin@teste.com", "ADMIN");

        mockMvc.perform(post("/orcamentos/{orcamentoId}/pagamentos", 4001L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "valor": 250.00,
                    "formaPagamento": "PIX",
                    "observacao": "Pagamento do admin"
                }
                """)).andExpect(status().is2xxSuccessful());


        Integer quantidadePagamentos = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM pagamento
                WHERE orcamento_id = 4001
                """, Integer.class);

        assertEquals(1, quantidadePagamentos);

        String statusPagamento = jdbcTemplate.queryForObject("""
                SELECT status
                FROM pagamento
                WHERE orcamento_id = 4001
                """, String.class);

        Long usuarioAuditoria = jdbcTemplate.queryForObject("""
                SELECT ph.usuario_id
                FROM pagamento_historico ph
                JOIN pagamento p
                    ON p.id = ph.pagamento_id
                WHERE p.orcamento_id = 4001
                """, Long.class);

        assertEquals(9001L, usuarioAuditoria);

        assertEquals("PENDENTE", statusPagamento);
    }

    @Test
    void devePermitirAtendenteCriarPagamento() throws Exception {

        String token = gerarToken("atendente@teste.com", "ATENDENTE");

        mockMvc.perform(post("/orcamentos/{orcamentoId}/pagamentos", 4001L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "valor": 300.00,
                    "formaPagamento": "PIX",
                    "observacao": "Pagamento do atendente"
                }
                """)).andExpect(status().is2xxSuccessful());

        Integer quantidadePagamentos = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM pagamento
                WHERE orcamento_id = 4001
                """, Integer.class);

        assertEquals(1, quantidadePagamentos);
    }
}