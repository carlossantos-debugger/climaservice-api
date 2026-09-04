package com.climaservice.api.integration;

import com.climaservice.api.dto.NotaFiscalServicoRequestDTO;
import com.climaservice.api.dto.NotaFiscalServicoResponseDTO;
import com.climaservice.api.dto.PageResponseDTO;
import com.climaservice.api.entity.StatusNotaFiscalServico;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.service.NotaFiscalServicoService;

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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NotaFiscalServicoMultiTenancyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private NotaFiscalServicoService notaFiscalServicoService;

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

        // Empresas, com cadastro fiscal completo
        jdbcTemplate.update("""
                INSERT INTO empresa (id, nome, cpf_cnpj, ativo, data_criacao, logradouro, numero, bairro, cidade, uf, cep, regime_tributario, codigo_servico_padrao, aliquota_iss_padrao)
                VALUES (8001, 'Empresa A', '11111111000191', true, CURRENT_TIMESTAMP, 'Rua A', '100', 'Centro', 'Brusque', 'SC', '88350000', 'SIMPLES_NACIONAL', '01.07', 5.00),
                       (8002, 'Empresa B', '22222222000192', true, CURRENT_TIMESTAMP, 'Rua B', '200', 'Centro', 'Brusque', 'SC', '88350000', 'SIMPLES_NACIONAL', '01.07', 5.00)
                """);

        jdbcTemplate.update("""
                INSERT INTO usuario (id, ativo, data_criacao, email, nome, role, senha_hash, empresa_id)
                VALUES (9001, true, CURRENT_TIMESTAMP, 'admin-a@teste.com', 'Administrador A', 'ADMIN', 'hash', 8001),
                       (9101, true, CURRENT_TIMESTAMP, 'admin-b@teste.com', 'Administrador B', 'ADMIN', 'hash', 8002)
                """);

        // Clientes, com cadastro fiscal completo
        jdbcTemplate.update("""
                INSERT INTO cliente (id, nome, cpf_cnpj, telefone, email, empresa_id, logradouro, numero, bairro, cidade, uf, cep)
                VALUES (1001, 'Cliente A', '11111111111', '47911111111', 'cliente-a@teste.com', 8001, 'Rua Cliente A', '10', 'Centro', 'Brusque', 'SC', '88350000'),
                       (1002, 'Cliente B', '22222222222', '47922222222', 'cliente-b@teste.com', 8002, 'Rua Cliente B', '20', 'Centro', 'Brusque', 'SC', '88350000')
                """);

        jdbcTemplate.update("""
                INSERT INTO equipamento (id, capacidade_btu, local_instalacao, marca, modelo, numero_serie, cliente_id, status, empresa_id)
                VALUES (2001, 12000, 'Sala', 'LG', 'Dual Inverter', 'SERIE-A', 1001, 'ATIVO', 8001),
                       (2002, 12000, 'Sala', 'LG', 'Dual Inverter', 'SERIE-B', 1002, 'ATIVO', 8002)
                """);

        // Ordens de serviço: 3001/3002 já têm nota; 3003 fica livre para os testes de criação
        jdbcTemplate.update("""
                INSERT INTO ordem_servico (id, data_abertura, descricao_problema, diagnostico, status, cliente_id, equipamento_id, empresa_id)
                VALUES (3001, CURRENT_TIMESTAMP, 'Problema A', NULL, 'CONCLUIDA', 1001, 2001, 8001),
                       (3002, CURRENT_TIMESTAMP, 'Problema B', NULL, 'CONCLUIDA', 1002, 2002, 8002),
                       (3003, CURRENT_TIMESTAMP, 'Problema A2', NULL, 'CONCLUIDA', 1001, 2001, 8001)
                """);

        jdbcTemplate.update("""
                INSERT INTO orcamento (id, ordem_servico_id, status, valor_total, data_criacao, empresa_id)
                VALUES (4001, 3001, 'APROVADO', 1000.00, CURRENT_TIMESTAMP, 8001),
                       (4002, 3002, 'APROVADO', 2000.00, CURRENT_TIMESTAMP, 8002),
                       (4003, 3003, 'APROVADO', 1500.00, CURRENT_TIMESTAMP, 8001)
                """);

        jdbcTemplate.update("""
                INSERT INTO nota_fiscal_servico (id, status, ambiente, discriminacao_servico, codigo_servico, aliquota_iss, valor_servico, valor_iss, data_criacao, ordem_servico_id, orcamento_id, empresa_id)
                VALUES (5001, 'RASCUNHO', 'HOMOLOGACAO', 'Manutenção A', '01.07', 5.00, 1000.00, 50.00, CURRENT_TIMESTAMP, 3001, 4001, 8001),
                       (5002, 'RASCUNHO', 'HOMOLOGACAO', 'Manutenção B', '01.07', 5.00, 2000.00, 100.00, CURRENT_TIMESTAMP, 3002, 4002, 8002)
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
    void deveListarSomenteNotasDaEmpresaAutenticada() {

        PageResponseDTO<NotaFiscalServicoResponseDTO> pagina = notaFiscalServicoService.listar(null, 0, 20);

        assertEquals(1, pagina.content().size());

        assertEquals(5001L, pagina.content().get(0).id());
    }

    @Test
    void deveOcultarNotaDeOutraEmpresa() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> notaFiscalServicoService.buscarPorId(5002L));

        assertEquals("Nota fiscal de serviço com ID 5002 não encontrada", exception.getMessage());
    }

    @Test
    void deveImpedirAtualizarNotaDeOutraEmpresa() {

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Tentativa", null, null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> notaFiscalServicoService.atualizar(5002L, dto));

        assertEquals("Nota fiscal de serviço com ID 5002 não encontrada", exception.getMessage());

        String discriminacaoAtual = jdbcTemplate.queryForObject("SELECT discriminacao_servico FROM nota_fiscal_servico WHERE id = 5002", String.class);

        assertEquals("Manutenção B", discriminacaoAtual);
    }

    @Test
    void deveImpedirCancelarNotaDeOutraEmpresa() {

        assertThrows(ResourceNotFoundException.class, () -> notaFiscalServicoService.cancelar(5002L));

        String status = jdbcTemplate.queryForObject("SELECT status FROM nota_fiscal_servico WHERE id = 5002", String.class);

        assertEquals("RASCUNHO", status);
    }

    @Test
    void deveImpedirGerarPayloadDeNotaDeOutraEmpresa() {

        assertThrows(ResourceNotFoundException.class, () -> notaFiscalServicoService.gerarPayload(5002L));
    }

    @Test
    void deveImpedirEnviarNotaDeOutraEmpresa() {

        assertThrows(ResourceNotFoundException.class, () -> notaFiscalServicoService.enviar(5002L));
    }

    @Test
    void deveImpedirCriarNotaParaOrdemServicoDeOutraEmpresa() {

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Tentativa entre empresas", null, null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> notaFiscalServicoService.criar(3002L, dto));

        assertEquals("Ordem de serviço com ID 3002 não encontrada", exception.getMessage());
    }

    @Test
    void devePermitirCriarNotaParaOrdemServicoDaPropriaEmpresa() {

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Instalação de novo equipamento", null, null);

        NotaFiscalServicoResponseDTO response = notaFiscalServicoService.criar(3003L, dto);

        assertNotNull(response.id());

        assertEquals(StatusNotaFiscalServico.RASCUNHO, response.status());

        assertEquals(new BigDecimal("1500.00"), response.valorServico());

        assertEquals("01.07", response.codigoServico());
    }

    @Test
    void deveImpedirSegundaNotaAtivaParaAMesmaOrdemServico() {

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Segunda tentativa", null, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> notaFiscalServicoService.criar(3001L, dto));

        assertEquals("Já existe uma nota fiscal ativa para esta ordem de serviço", exception.getMessage());
    }

    @Test
    void deveMudarIsolamentoQuandoUsuarioDaEmpresaBForAutenticado() {

        autenticar("admin-b@teste.com", "ADMIN");

        PageResponseDTO<NotaFiscalServicoResponseDTO> pagina = notaFiscalServicoService.listar(null, 0, 20);

        assertEquals(1, pagina.content().size());

        assertEquals(5002L, pagina.content().get(0).id());
    }
}
