package com.climaservice.api.integration;

import com.climaservice.api.dto.AgendamentoRequestDTO;
import com.climaservice.api.dto.AgendamentoResponseDTO;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.service.AgendamentoService;

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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AgendamentoMultiTenancyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AgendamentoService agendamentoService;

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
                    (9003, false, CURRENT_TIMESTAMP, 'tecnico-a-inativo@teste.com', 'Técnico A Inativo', 'TECNICO', 'hash', 8001),
                    (9004, true, CURRENT_TIMESTAMP, 'atendente-a@teste.com', 'Atendente A', 'ATENDENTE', 'hash', 8001),
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

        // Ordens de serviço
        jdbcTemplate.update("""
                INSERT INTO ordem_servico (id, data_abertura, descricao_problema, diagnostico, status, cliente_id, equipamento_id, empresa_id)
                VALUES (3001, CURRENT_TIMESTAMP, 'Problema A', NULL, 'ABERTA', 1001, 2001, 8001),
                       (3002, CURRENT_TIMESTAMP, 'Problema B', NULL, 'ABERTA', 1002, 2002, 8002)
                """);

        // Agendamento já existente da Empresa A: 2026-09-01 09:00 - 10:00
        jdbcTemplate.update("""
                INSERT INTO agendamento (id, data_hora_inicio, data_hora_fim, status, observacao, data_criacao, ordem_servico_id, tecnico_id, empresa_id)
                VALUES (5001, '2026-09-01 09:00:00', '2026-09-01 10:00:00', 'AGENDADO', 'Agendamento A', CURRENT_TIMESTAMP, 3001, 9002, 8001)
                """);

        // Agendamento da Empresa B
        jdbcTemplate.update("""
                INSERT INTO agendamento (id, data_hora_inicio, data_hora_fim, status, observacao, data_criacao, ordem_servico_id, tecnico_id, empresa_id)
                VALUES (5002, '2026-09-01 09:00:00', '2026-09-01 10:00:00', 'AGENDADO', 'Agendamento B', CURRENT_TIMESTAMP, 3002, 9102, 8002)
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
    void deveListarSomenteAgendamentosDaEmpresaAutenticada() {

        List<AgendamentoResponseDTO> agendamentos = agendamentoService.listar(null, null, null, null);

        assertEquals(1, agendamentos.size());

        assertEquals(5001L, agendamentos.get(0).id());
    }

    @Test
    void deveOcultarAgendamentoDeOutraEmpresa() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> agendamentoService.buscarPorId(5002L));

        assertEquals("Agendamento com ID 5002 não encontrado", exception.getMessage());
    }

    @Test
    void deveImpedirCriarAgendamentoParaOrdemServicoDeOutraEmpresa() {

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(3002L, 9002L, LocalDateTime.of(2026, 9, 2, 9, 0), LocalDateTime.of(2026, 9, 2, 10, 0), null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> agendamentoService.criar(dto));

        assertEquals("Ordem de serviço com ID 3002 não encontrada", exception.getMessage());
    }

    @Test
    void deveImpedirCriarAgendamentoParaTecnicoDeOutraEmpresa() {

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(3001L, 9102L, LocalDateTime.of(2026, 9, 2, 9, 0), LocalDateTime.of(2026, 9, 2, 10, 0), null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> agendamentoService.criar(dto));

        assertEquals("Técnico com ID 9102 não encontrado", exception.getMessage());
    }

    @Test
    void deveImpedirAgendamentoParaTecnicoInativo() {

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(3001L, 9003L, LocalDateTime.of(2026, 9, 2, 9, 0), LocalDateTime.of(2026, 9, 2, 10, 0), null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> agendamentoService.criar(dto));

        assertEquals("O técnico informado está inativo", exception.getMessage());
    }

    @Test
    void deveImpedirSobreposicaoDeHorarioComDadosReais() {

        // Já existe 09:00-10:00 para o técnico 9002. Tenta 09:30-10:30.
        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(3001L, 9002L, LocalDateTime.of(2026, 9, 1, 9, 30), LocalDateTime.of(2026, 9, 1, 10, 30), null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> agendamentoService.criar(dto));

        assertEquals("O técnico já possui um agendamento nesse intervalo de horário", exception.getMessage());
    }

    @Test
    void devePermitirAgendamentoAdjacenteSemSobreposicao() {

        // Existente termina às 10:00. Novo começa exatamente às 10:00.
        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(3001L, 9002L, LocalDateTime.of(2026, 9, 1, 10, 0), LocalDateTime.of(2026, 9, 1, 11, 0), null);

        AgendamentoResponseDTO response = agendamentoService.criar(dto);

        assertNotNull(response.id());
    }

    @Test
    void devePermitirAgendamentoParaOutroTecnicoNoMesmoHorario() {

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(3001L, 9004L, LocalDateTime.of(2026, 9, 1, 9, 0), LocalDateTime.of(2026, 9, 1, 10, 0), null);

        // 9004 é ATENDENTE, não TECNICO — deve falhar por perfil, não por sobreposição.
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> agendamentoService.criar(dto));

        assertEquals("O usuário informado não possui perfil de técnico", exception.getMessage());
    }

    @Test
    void deveImpedirListagemPorOrdemServicoDeOutroTenant() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> agendamentoService.listarPorOrdemServico(3002L));

        assertEquals("Ordem de serviço com ID 3002 não encontrada", exception.getMessage());
    }

    @Test
    void deveImpedirListagemPorTecnicoDeOutroTenant() {

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> agendamentoService.listarPorTecnico(9102L));

        assertEquals("Técnico com ID 9102 não encontrado", exception.getMessage());
    }

    @Test
    void deveMudarIsolamentoQuandoUsuarioDaEmpresaBForAutenticado() {

        autenticar("admin-b@teste.com", "ADMIN");

        List<AgendamentoResponseDTO> agendamentos = agendamentoService.listar(null, null, null, null);

        assertEquals(1, agendamentos.size());

        assertEquals(5002L, agendamentos.get(0).id());
    }
}
