package com.climaservice.api.integration;

import com.climaservice.api.dto.PlanoManutencaoPreventivaAtualizarRequestDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaExecucaoResponseDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaResponseDTO;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.service.PlanoManutencaoPreventivaService;

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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PlanoManutencaoPreventivaMultiTenancyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PlanoManutencaoPreventivaService planoService;

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

        // Empresas
        jdbcTemplate.update("""
                INSERT INTO empresa (id, nome, cpf_cnpj, ativo, data_criacao)
                VALUES (8001, 'Empresa A', NULL, true, CURRENT_TIMESTAMP),
                       (8002, 'Empresa B', NULL, true, CURRENT_TIMESTAMP)
                """);

        // Usuários
        jdbcTemplate.update("""
                INSERT INTO usuario (id, ativo, data_criacao, email, nome, role, senha_hash, empresa_id)
                VALUES
                    (9001, true, CURRENT_TIMESTAMP, 'admin-a@teste.com', 'Administrador A', 'ADMIN', 'hash', 8001),
                    (9002, true, CURRENT_TIMESTAMP, 'tecnico-a@teste.com', 'Técnico A', 'TECNICO', 'hash', 8001),
                    (9101, true, CURRENT_TIMESTAMP, 'admin-b@teste.com', 'Administrador B', 'ADMIN', 'hash', 8002),
                    (9102, true, CURRENT_TIMESTAMP, 'tecnico-b@teste.com', 'Técnico B', 'TECNICO', 'hash', 8002)
                """);

        // Clientes
        jdbcTemplate.update("""
                INSERT INTO cliente (id, nome, cpf_cnpj, telefone, email, empresa_id)
                VALUES (1001, 'Cliente A', '11111111111', '47911111111', 'cliente-a@teste.com', 8001),
                       (1002, 'Cliente B', '22222222222', '47922222222', 'cliente-b@teste.com', 8002)
                """);

        // Equipamentos
        jdbcTemplate.update("""
                INSERT INTO equipamento (id, capacidade_btu, local_instalacao, marca, modelo, numero_serie, cliente_id, status, empresa_id)
                VALUES (2001, 12000, 'Sala', 'LG', 'Dual Inverter', 'SERIE-A', 1001, 'ATIVO', 8001),
                       (2002, 12000, 'Sala', 'LG', 'Dual Inverter', 'SERIE-B', 1002, 'ATIVO', 8002)
                """);

        // Planos de manutenção preventiva: A vencido hoje, B ainda não vencido
        jdbcTemplate.update("""
                INSERT INTO plano_manutencao_preventiva (id, intervalo_meses, proxima_execucao, ultima_execucao, ativo, observacao, data_criacao, equipamento_id, tecnico_padrao_id, empresa_id)
                VALUES (6001, 6, CURRENT_DATE, NULL, true, 'Plano A', CURRENT_TIMESTAMP, 2001, 9002, 8001),
                       (6002, 3, CURRENT_DATE + 30, NULL, true, 'Plano B', CURRENT_TIMESTAMP, 2002, NULL, 8002)
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
    void deveListarSomentePlanosDaEmpresaAutenticada() {

        List<PlanoManutencaoPreventivaResponseDTO> planos = planoService.listar(null, null);

        assertEquals(1, planos.size());

        assertEquals(6001L, planos.get(0).id());
    }

    @Test
    void deveOcultarPlanoDeOutraEmpresa() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> planoService.buscarPorId(6002L));

        assertEquals("Plano de manutenção preventiva com ID 6002 não encontrado", exception.getMessage());
    }

    @Test
    void deveImpedirAtualizarPlanoDeOutraEmpresa() {

        PlanoManutencaoPreventivaAtualizarRequestDTO dto = new PlanoManutencaoPreventivaAtualizarRequestDTO(null, 12, LocalDate.now().plusMonths(1), "Tentativa");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> planoService.atualizar(6002L, dto));

        assertEquals("Plano de manutenção preventiva com ID 6002 não encontrado", exception.getMessage());

        Integer intervaloAtual = jdbcTemplate.queryForObject("SELECT intervalo_meses FROM plano_manutencao_preventiva WHERE id = 6002", Integer.class);

        assertEquals(3, intervaloAtual);
    }

    @Test
    void deveImpedirAtivarEInativarPlanoDeOutraEmpresa() {

        assertThrows(ResourceNotFoundException.class, () -> planoService.ativar(6002L));

        assertThrows(ResourceNotFoundException.class, () -> planoService.inativar(6002L));

        Boolean ativo = jdbcTemplate.queryForObject("SELECT ativo FROM plano_manutencao_preventiva WHERE id = 6002", Boolean.class);

        assertTrue(ativo);
    }

    @Test
    void deveImpedirGerarOrdemServicoParaPlanoDeOutraEmpresa() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> planoService.gerarOrdemServico(6002L));

        assertEquals("Plano de manutenção preventiva com ID 6002 não encontrado", exception.getMessage());
    }

    @Test
    void deveImpedirListarExecucoesDePlanoDeOutraEmpresa() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> planoService.listarExecucoes(6002L));

        assertEquals("Plano de manutenção preventiva com ID 6002 não encontrado", exception.getMessage());
    }

    @Test
    void deveImpedirListagemPorEquipamentoDeOutroTenant() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> planoService.listarPorEquipamento(2002L));

        assertEquals("Equipamento com ID 2002 não encontrado", exception.getMessage());
    }

    @Test
    void devePermitirGerarOrdemServicoParaPlanoDaPropriaEmpresa() {

        PlanoManutencaoPreventivaExecucaoResponseDTO execucao = planoService.gerarOrdemServico(6001L);

        assertNotNull(execucao);

        assertNotNull(execucao.ordemServicoId());

        assertEquals(6001L, execucao.planoId());

        LocalDate novaProximaExecucao = jdbcTemplate.queryForObject("SELECT proxima_execucao FROM plano_manutencao_preventiva WHERE id = 6001", LocalDate.class);

        assertEquals(LocalDate.now().plusMonths(6), novaProximaExecucao);

        LocalDate ultimaExecucao = jdbcTemplate.queryForObject("SELECT ultima_execucao FROM plano_manutencao_preventiva WHERE id = 6001", LocalDate.class);

        assertEquals(LocalDate.now(), ultimaExecucao);

        Integer quantidadeOs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ordem_servico WHERE id = ?", Integer.class, execucao.ordemServicoId());

        assertEquals(1, quantidadeOs);
    }

    @Test
    void deveImpedirSegundaGeracaoImediataParaAMesmaOcorrencia() {

        /*
         * Ao gerar com sucesso, o plano avança proximaExecucao. Uma
         * segunda chamada imediata não pode criar uma segunda OS para
         * a mesma ocorrência — nem por já ter sido registrada, nem
         * por estar fora do prazo depois de avançar.
         */
        planoService.gerarOrdemServico(6001L);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> planoService.gerarOrdemServico(6001L));

        assertEquals("A manutenção preventiva ainda não está no prazo de execução", exception.getMessage());

        Integer quantidadeExecucoes = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM plano_manutencao_preventiva_execucao WHERE plano_manutencao_preventiva_id = 6001", Integer.class);

        assertEquals(1, quantidadeExecucoes);
    }

    @Test
    void deveMudarIsolamentoQuandoUsuarioDaEmpresaBForAutenticado() {

        autenticar("admin-b@teste.com", "ADMIN");

        List<PlanoManutencaoPreventivaResponseDTO> planos = planoService.listar(null, null);

        assertEquals(1, planos.size());

        assertEquals(6002L, planos.get(0).id());
    }
}
