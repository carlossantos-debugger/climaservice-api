package com.climaservice.api.service;

import com.climaservice.api.dto.AgendamentoReagendarRequestDTO;
import com.climaservice.api.dto.AgendamentoRequestDTO;
import com.climaservice.api.dto.AgendamentoResponseDTO;
import com.climaservice.api.dto.AtualizarStatusAgendamentoRequestDTO;
import com.climaservice.api.entity.*;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.AgendamentoHistoricoRepository;
import com.climaservice.api.repository.AgendamentoRepository;
import com.climaservice.api.repository.OrdemServicoRepository;
import com.climaservice.api.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    private static final Long EMPRESA_ID = 8001L;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendamentoHistoricoRepository historicoRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private Empresa empresa;

    @Mock
    private OrdemServico ordemServico;

    @Mock
    private Usuario tecnico;

    @Mock
    private Usuario usuarioAtual;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private final LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 9, 0);
    private final LocalDateTime fim = LocalDateTime.of(2026, 9, 1, 10, 0);


    private void prepararEmpresaAtual() {

        when(usuarioAutenticadoService.obterEmpresaAtual()).thenReturn(empresa);

        when(empresa.getId()).thenReturn(EMPRESA_ID);
    }

    private void prepararOrdemServicoValida(Long ordemServicoId) {

        prepararEmpresaAtual();

        when(ordemServicoRepository.findByIdAndEmpresa_Id(ordemServicoId, EMPRESA_ID)).thenReturn(Optional.of(ordemServico));

        when(ordemServico.getStatus()).thenReturn(StatusOrdemServico.ABERTA);
    }

    private void prepararTecnicoValido(Long tecnicoId) {

        when(usuarioRepository.findByIdAndEmpresa_Id(tecnicoId, EMPRESA_ID)).thenReturn(Optional.of(tecnico));

        when(tecnico.getRole()).thenReturn(RoleUsuario.TECNICO);

        when(tecnico.getAtivo()).thenReturn(true);
    }


    @Test
    void deveCriarAgendamentoQuandoDadosForemValidos() {

        prepararOrdemServicoValida(1L);

        prepararTecnicoValido(2L);

        when(tecnico.getId()).thenReturn(2L);

        when(tecnico.getNome()).thenReturn("Técnico Teste");

        when(agendamentoRepository.buscarConflitantes(eq(2L), eq(EMPRESA_ID), any(), eq(inicio), eq(fim), isNull())).thenReturn(List.of());

        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(usuarioAutenticadoService.obterUsuarioAtual()).thenReturn(usuarioAtual);

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(1L, 2L, inicio, fim, "Observação");

        AgendamentoResponseDTO response = agendamentoService.criar(dto);

        assertNotNull(response);

        assertEquals(StatusAgendamento.AGENDADO, response.status());

        assertEquals(2L, response.tecnicoId());

        assertEquals("Técnico Teste", response.tecnicoNome());

        verify(agendamentoRepository).save(any(Agendamento.class));

        verify(historicoRepository).save(any(AgendamentoHistorico.class));
    }


    @Test
    void deveImpedirAgendamentoParaOrdemServicoCancelada() {

        prepararOrdemServicoValida(1L);

        when(ordemServico.getStatus()).thenReturn(StatusOrdemServico.CANCELADA);

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(1L, 2L, inicio, fim, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> agendamentoService.criar(dto));

        assertEquals("Não é possível agendar uma ordem de serviço cancelada", exception.getMessage());

        verify(agendamentoRepository, never()).save(any());
    }


    @Test
    void deveImpedirAgendamentoParaOrdemServicoConcluida() {

        prepararOrdemServicoValida(1L);

        when(ordemServico.getStatus()).thenReturn(StatusOrdemServico.CONCLUIDA);

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(1L, 2L, inicio, fim, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> agendamentoService.criar(dto));

        assertEquals("Não é possível agendar uma ordem de serviço concluída", exception.getMessage());

        verify(agendamentoRepository, never()).save(any());
    }


    @Test
    void deveLancarExcecaoQuandoOrdemServicoNaoExistir() {

        prepararEmpresaAtual();

        when(ordemServicoRepository.findByIdAndEmpresa_Id(999L, EMPRESA_ID)).thenReturn(Optional.empty());

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(999L, 2L, inicio, fim, null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> agendamentoService.criar(dto));

        assertEquals("Ordem de serviço com ID 999 não encontrada", exception.getMessage());

        verifyNoInteractions(usuarioRepository);
    }


    @Test
    void deveImpedirAgendamentoParaUsuarioQueNaoETecnico() {

        prepararOrdemServicoValida(1L);

        when(usuarioRepository.findByIdAndEmpresa_Id(2L, EMPRESA_ID)).thenReturn(Optional.of(tecnico));

        when(tecnico.getRole()).thenReturn(RoleUsuario.ATENDENTE);

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(1L, 2L, inicio, fim, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> agendamentoService.criar(dto));

        assertEquals("O usuário informado não possui perfil de técnico", exception.getMessage());

        verify(agendamentoRepository, never()).save(any());
    }


    @Test
    void deveImpedirAgendamentoParaTecnicoInativo() {

        prepararOrdemServicoValida(1L);

        when(usuarioRepository.findByIdAndEmpresa_Id(2L, EMPRESA_ID)).thenReturn(Optional.of(tecnico));

        when(tecnico.getRole()).thenReturn(RoleUsuario.TECNICO);

        when(tecnico.getAtivo()).thenReturn(false);

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(1L, 2L, inicio, fim, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> agendamentoService.criar(dto));

        assertEquals("O técnico informado está inativo", exception.getMessage());

        verify(agendamentoRepository, never()).save(any());
    }


    @Test
    void deveLancarExcecaoQuandoTecnicoNaoExistir() {

        prepararOrdemServicoValida(1L);

        when(usuarioRepository.findByIdAndEmpresa_Id(999L, EMPRESA_ID)).thenReturn(Optional.empty());

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(1L, 999L, inicio, fim, null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> agendamentoService.criar(dto));

        assertEquals("Técnico com ID 999 não encontrado", exception.getMessage());

        verify(agendamentoRepository, never()).save(any());
    }


    @Test
    void deveImpedirAgendamentoComDataFimAnteriorOuIgualADataInicio() {

        prepararOrdemServicoValida(1L);

        prepararTecnicoValido(2L);

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(1L, 2L, inicio, inicio, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> agendamentoService.criar(dto));

        assertEquals("A data/hora de fim deve ser posterior à data/hora de início", exception.getMessage());

        verify(agendamentoRepository, never()).save(any());
    }


    @Test
    void deveImpedirAgendamentoComSobreposicaoDeHorarioParaOMesmoTecnico() {

        prepararOrdemServicoValida(1L);

        prepararTecnicoValido(2L);

        when(tecnico.getId()).thenReturn(2L);

        Agendamento conflitante = mock(Agendamento.class);

        when(agendamentoRepository.buscarConflitantes(eq(2L), eq(EMPRESA_ID), any(), eq(inicio), eq(fim), isNull())).thenReturn(List.of(conflitante));

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(1L, 2L, inicio, fim, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> agendamentoService.criar(dto));

        assertEquals("O técnico já possui um agendamento nesse intervalo de horário", exception.getMessage());

        verify(agendamentoRepository, never()).save(any());
    }


    @Test
    void deveBuscarAgendamentoPorId() {

        prepararEmpresaAtual();

        Agendamento agendamento = mock(Agendamento.class);

        when(agendamentoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));

        when(agendamento.getOrdemServico()).thenReturn(ordemServico);

        when(agendamento.getTecnico()).thenReturn(tecnico);

        when(agendamento.getStatus()).thenReturn(StatusAgendamento.AGENDADO);

        when(ordemServico.getId()).thenReturn(1L);

        when(tecnico.getId()).thenReturn(2L);

        when(tecnico.getNome()).thenReturn("Técnico Teste");

        AgendamentoResponseDTO response = agendamentoService.buscarPorId(1L);

        assertEquals(1L, response.ordemServicoId());

        assertEquals(2L, response.tecnicoId());

        assertEquals(StatusAgendamento.AGENDADO, response.status());
    }


    @Test
    void deveLancarExcecaoQuandoAgendamentoNaoExistir() {

        prepararEmpresaAtual();

        when(agendamentoRepository.findByIdAndEmpresa_Id(999L, EMPRESA_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> agendamentoService.buscarPorId(999L));

        assertEquals("Agendamento com ID 999 não encontrado", exception.getMessage());
    }


    @Test
    void deveImpedirListagemPorOrdemServicoDeOutraEmpresa() {

        prepararEmpresaAtual();

        when(ordemServicoRepository.findByIdAndEmpresa_Id(50L, EMPRESA_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> agendamentoService.listarPorOrdemServico(50L));

        assertEquals("Ordem de serviço com ID 50 não encontrada", exception.getMessage());

        verifyNoInteractions(agendamentoRepository);
    }


    @Test
    void deveImpedirListagemPorTecnicoDeOutraEmpresa() {

        prepararEmpresaAtual();

        when(usuarioRepository.findByIdAndEmpresa_Id(50L, EMPRESA_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> agendamentoService.listarPorTecnico(50L));

        assertEquals("Técnico com ID 50 não encontrado", exception.getMessage());

        verifyNoInteractions(agendamentoRepository);
    }


    @Test
    void deveConfirmarAgendamentoAgendado() {

        prepararEmpresaAtual();

        Agendamento agendamento = mock(Agendamento.class);

        when(agendamentoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));

        when(agendamento.getStatus()).thenReturn(StatusAgendamento.AGENDADO);

        when(agendamento.getOrdemServico()).thenReturn(ordemServico);

        when(agendamento.getTecnico()).thenReturn(tecnico);

        when(ordemServico.getId()).thenReturn(1L);

        when(tecnico.getId()).thenReturn(2L);

        when(tecnico.getNome()).thenReturn("Técnico Teste");

        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        when(usuarioAutenticadoService.obterUsuarioAtual()).thenReturn(usuarioAtual);

        AtualizarStatusAgendamentoRequestDTO dto = new AtualizarStatusAgendamentoRequestDTO(StatusAgendamento.CONFIRMADO);

        agendamentoService.atualizarStatus(1L, dto);

        verify(agendamento).setStatus(StatusAgendamento.CONFIRMADO);

        verify(historicoRepository).save(any(AgendamentoHistorico.class));
    }


    @Test
    void deveImpedirTransicaoInvalidaDeStatus() {

        prepararEmpresaAtual();

        Agendamento agendamento = mock(Agendamento.class);

        when(agendamentoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));

        when(agendamento.getStatus()).thenReturn(StatusAgendamento.AGENDADO);

        AtualizarStatusAgendamentoRequestDTO dto = new AtualizarStatusAgendamentoRequestDTO(StatusAgendamento.CONCLUIDO);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> agendamentoService.atualizarStatus(1L, dto));

        assertEquals("Transição de status inválida: AGENDADO -> CONCLUIDO", exception.getMessage());

        verify(agendamento, never()).setStatus(any());
    }


    @Test
    void deveImpedirAlteracaoDeStatusAPartirDeEstadoFinal() {

        prepararEmpresaAtual();

        Agendamento agendamento = mock(Agendamento.class);

        when(agendamentoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));

        when(agendamento.getStatus()).thenReturn(StatusAgendamento.CANCELADO);

        AtualizarStatusAgendamentoRequestDTO dto = new AtualizarStatusAgendamentoRequestDTO(StatusAgendamento.CONFIRMADO);

        assertThrows(BusinessRuleException.class, () -> agendamentoService.atualizarStatus(1L, dto));

        verify(agendamento, never()).setStatus(any());
    }


    @Test
    void deveReagendarAgendamentoAgendado() {

        prepararEmpresaAtual();

        Agendamento agendamento = mock(Agendamento.class);

        when(agendamentoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));

        when(agendamento.getStatus()).thenReturn(StatusAgendamento.AGENDADO);

        when(agendamento.getTecnico()).thenReturn(tecnico);

        when(agendamento.getEmpresa()).thenReturn(empresa);

        when(agendamento.getOrdemServico()).thenReturn(ordemServico);

        when(agendamento.getId()).thenReturn(1L);

        when(tecnico.getId()).thenReturn(2L);

        when(tecnico.getNome()).thenReturn("Técnico Teste");

        when(ordemServico.getId()).thenReturn(1L);

        LocalDateTime novoInicio = inicio.plusDays(1);

        LocalDateTime novoFim = fim.plusDays(1);

        when(agendamentoRepository.buscarConflitantes(eq(2L), eq(EMPRESA_ID), any(), eq(novoInicio), eq(novoFim), eq(1L))).thenReturn(List.of());

        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        AgendamentoReagendarRequestDTO dto = new AgendamentoReagendarRequestDTO(novoInicio, novoFim);

        agendamentoService.reagendar(1L, dto);

        verify(agendamento).setDataHoraInicio(novoInicio);

        verify(agendamento).setDataHoraFim(novoFim);
    }


    @Test
    void deveImpedirReagendarAgendamentoConcluido() {

        prepararEmpresaAtual();

        Agendamento agendamento = mock(Agendamento.class);

        when(agendamentoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));

        when(agendamento.getStatus()).thenReturn(StatusAgendamento.CONCLUIDO);

        AgendamentoReagendarRequestDTO dto = new AgendamentoReagendarRequestDTO(inicio, fim);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> agendamentoService.reagendar(1L, dto));

        assertEquals("Não é possível reagendar um agendamento concluído ou cancelado", exception.getMessage());

        verify(agendamentoRepository, never()).save(any());
    }


    @Test
    void deveListarAgendamentosComFiltros() {

        prepararEmpresaAtual();

        Agendamento agendamento = mock(Agendamento.class);

        when(agendamento.getOrdemServico()).thenReturn(ordemServico);

        when(agendamento.getTecnico()).thenReturn(tecnico);

        when(ordemServico.getId()).thenReturn(1L);

        when(tecnico.getId()).thenReturn(2L);

        when(tecnico.getNome()).thenReturn("Técnico Teste");

        when(agendamento.getStatus()).thenReturn(StatusAgendamento.AGENDADO);

        when(agendamentoRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(agendamento));

        List<AgendamentoResponseDTO> resultado = agendamentoService.listar(inicio, fim, 2L, StatusAgendamento.AGENDADO);

        assertEquals(1, resultado.size());

        assertEquals(2L, resultado.get(0).tecnicoId());
    }
}
