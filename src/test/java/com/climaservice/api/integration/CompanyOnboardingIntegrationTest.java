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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyOnboardingIntegrationTest extends AbstractIntegrationTest {

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

        // Empresa A
        jdbcTemplate.update("""
                INSERT INTO empresa (id, nome, cpf_cnpj, ativo, data_criacao)
                VALUES (8001, 'Empresa A', '11111111000191', true, CURRENT_TIMESTAMP)
                """);

        // Empresa B
        jdbcTemplate.update("""
                INSERT INTO empresa (id, nome, cpf_cnpj, ativo, data_criacao)
                VALUES (8002, 'Empresa B', '22222222000192', true, CURRENT_TIMESTAMP)
                """);

        jdbcTemplate.update("""
                INSERT INTO usuario (id, ativo, data_criacao, email, nome, role, senha_hash, empresa_id)
                VALUES
                    (9001, true, CURRENT_TIMESTAMP, 'admin-a@teste.com', 'Administrador Empresa A', 'ADMIN', 'hash-teste', 8001),
                    (9002, true, CURRENT_TIMESTAMP, 'tecnico-a@teste.com', 'Tecnico Empresa A', 'TECNICO', 'hash-teste', 8001),
                    (9003, true, CURRENT_TIMESTAMP, 'admin-b@teste.com', 'Administrador Empresa B', 'ADMIN', 'hash-teste', 8002)
                """);
    }

    private String gerarToken(String email, String role) {

        SecretKey chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_TEST));

        Instant agora = Instant.now();

        return Jwts.builder().subject(email).claim("role", role).issuedAt(Date.from(agora)).expiration(Date.from(agora.plusSeconds(3600))).signWith(chave).compact();
    }

    @Test
    void deveRegistrarNovaEmpresaComAdminEGerarToken() throws Exception {

        mockMvc.perform(post("/auth/register-company").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "empresaNome": "Empresa Onboarding",
                    "empresaCpfCnpj": "33333333000193",
                    "adminNome": "Admin Onboarding",
                    "adminEmail": "admin@onboarding.com",
                    "adminSenha": "senhaSegura123"
                }
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.empresaId", notNullValue())).andExpect(jsonPath("$.empresaNome").value("Empresa Onboarding")).andExpect(jsonPath("$.usuarioEmail").value("admin@onboarding.com")).andExpect(jsonPath("$.role").value("ADMIN")).andExpect(jsonPath("$.token", notNullValue()));

        Boolean empresaAtiva = jdbcTemplate.queryForObject("""
                SELECT ativo FROM empresa WHERE cpf_cnpj = '33333333000193'
                """, Boolean.class);

        assertEquals(true, empresaAtiva);

        String senhaHash = jdbcTemplate.queryForObject("""
                SELECT senha_hash FROM usuario WHERE email = 'admin@onboarding.com'
                """, String.class);

        assertNotEquals("senhaSegura123", senhaHash);

        String role = jdbcTemplate.queryForObject("""
                SELECT role FROM usuario WHERE email = 'admin@onboarding.com'
                """, String.class);

        assertEquals("ADMIN", role);
    }

    @Test
    void devePermitirCadastroDeEmpresaSemCpfCnpj() throws Exception {

        mockMvc.perform(post("/auth/register-company").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "empresaNome": "Empresa Sem Documento",
                    "adminNome": "Admin Sem Documento",
                    "adminEmail": "admin@semdocumento.com",
                    "adminSenha": "senhaSegura123"
                }
                """)).andExpect(status().isCreated());
    }

    @Test
    void deveImpedirCadastroComEmailDeAdminJaExistente() throws Exception {

        mockMvc.perform(post("/auth/register-company").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "empresaNome": "Outra Empresa",
                    "adminNome": "Outro Admin",
                    "adminEmail": "admin-a@teste.com",
                    "adminSenha": "senhaSegura123"
                }
                """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Já existe um usuário cadastrado com este e-mail"));

        Integer quantidadeEmpresas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM empresa WHERE nome = 'Outra Empresa'
                """, Integer.class);

        assertEquals(0, quantidadeEmpresas);
    }

    @Test
    void deveImpedirCadastroComCpfCnpjDeEmpresaJaExistente() throws Exception {

        mockMvc.perform(post("/auth/register-company").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "empresaNome": "Empresa Duplicada",
                    "empresaCpfCnpj": "11111111000191",
                    "adminNome": "Admin Duplicado",
                    "adminEmail": "admin@duplicado.com",
                    "adminSenha": "senhaSegura123"
                }
                """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Já existe uma empresa cadastrada com este CPF/CNPJ"));
    }

    @Test
    void deveRetornar400QuandoDadosObrigatoriosNaoForemInformados() throws Exception {

        mockMvc.perform(post("/auth/register-company").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "empresaNome": "",
                    "adminNome": "Admin",
                    "adminEmail": "email-invalido",
                    "adminSenha": "123"
                }
                """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.empresaNome", notNullValue())).andExpect(jsonPath("$.fields.adminEmail", notNullValue())).andExpect(jsonPath("$.fields.adminSenha", notNullValue()));
    }

    @Test
    void deveRetornar401QuandoConsultarEmpresaSemToken() throws Exception {

        mockMvc.perform(get("/empresa/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAdminConsultarPropriaEmpresa() throws Exception {

        String token = gerarToken("admin-a@teste.com", "ADMIN");

        mockMvc.perform(get("/empresa/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(8001)).andExpect(jsonPath("$.nome").value("Empresa A"));
    }

    @Test
    void deveIsolarEmpresaMeEntreTenants() throws Exception {

        String tokenAdminB = gerarToken("admin-b@teste.com", "ADMIN");

        mockMvc.perform(get("/empresa/me").header("Authorization", "Bearer " + tokenAdminB)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(8002)).andExpect(jsonPath("$.nome").value("Empresa B"));
    }

    @Test
    void devePermitirAdminAtualizarPropriaEmpresa() throws Exception {

        String token = gerarToken("admin-a@teste.com", "ADMIN");

        mockMvc.perform(patch("/empresa/me").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "nome": "Empresa A Renomeada",
                    "cpfCnpj": "11111111000191"
                }
                """)).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Empresa A Renomeada"));

        String nomeAtualizado = jdbcTemplate.queryForObject("""
                SELECT nome FROM empresa WHERE id = 8001
                """, String.class);

        assertEquals("Empresa A Renomeada", nomeAtualizado);
    }

    @Test
    void deveNegarAtualizacaoDeEmpresaParaTecnico() throws Exception {

        String token = gerarToken("tecnico-a@teste.com", "TECNICO");

        mockMvc.perform(patch("/empresa/me").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "nome": "Tentativa Tecnico",
                    "cpfCnpj": "11111111000191"
                }
                """)).andExpect(status().isForbidden());

        String nomeAtual = jdbcTemplate.queryForObject("""
                SELECT nome FROM empresa WHERE id = 8001
                """, String.class);

        assertEquals("Empresa A", nomeAtual);
    }

    @Test
    void deveImpedirAtualizacaoComCpfCnpjJaUsadoPorOutraEmpresa() throws Exception {

        String token = gerarToken("admin-a@teste.com", "ADMIN");

        mockMvc.perform(patch("/empresa/me").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "nome": "Empresa A",
                    "cpfCnpj": "22222222000192"
                }
                """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Já existe uma empresa cadastrada com este CPF/CNPJ"));
    }

    @Test
    void devePermitirAdminAtualizarPropriaEmpresaMantendoOMesmoCpfCnpj() throws Exception {

        String token = gerarToken("admin-a@teste.com", "ADMIN");

        mockMvc.perform(patch("/empresa/me").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "nome": "Empresa A",
                    "cpfCnpj": "11111111000191"
                }
                """)).andExpect(status().isOk());
    }
}
