package com.climaservice.api.service;

import com.climaservice.api.dto.AgendamentoHistoricoResponseDTO;
import com.climaservice.api.dto.AgendamentoReagendarRequestDTO;
import com.climaservice.api.dto.AgendamentoRequestDTO;
import com.climaservice.api.dto.AgendamentoResponseDTO;
import com.climaservice.api.dto.AtualizarStatusAgendamentoRequestDTO;
import com.climaservice.api.dto.PageResponseDTO;
import com.climaservice.api.entity.Agendamento;
import com.climaservice.api.entity.AgendamentoHistorico;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.OrdemServico;
import com.climaservice.api.entity.RoleUsuario;
import com.climaservice.api.entity.StatusAgendamento;
import com.climaservice.api.entity.StatusOrdemServico;
import com.climaservice.api.entity.Usuario;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.AgendamentoHistoricoRepository;
import com.climaservice.api.repository.AgendamentoRepository;
import com.climaservice.api.repository.AgendamentoSpecifications;
import com.climaservice.api.repository.OrdemServicoRepository;
import com.climaservice.api.repository.UsuarioRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoService {

    private static final List<StatusAgendamento> STATUS_ATIVOS = List.of(StatusAgendamento.AGENDADO, StatusAgendamento.CONFIRMADO, StatusAgendamento.EM_ATENDIMENTO);

    private static final int LIMITE_PROXIMOS_AGENDAMENTOS = 10;

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoHistoricoRepository historicoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, AgendamentoHistoricoRepository historicoRepository, OrdemServicoRepository ordemServicoRepository, UsuarioRepository usuarioRepository, UsuarioAutenticadoService usuarioAutenticadoService) {

        this.agendamentoRepository = agendamentoRepository;
        this.historicoRepository = historicoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public AgendamentoResponseDTO criar(AgendamentoRequestDTO dto) {

        Empresa empresa = usuarioAutenticadoService.obterEmpresaAtual();

        Long empresaId = empresa.getId();

        OrdemServico ordemServico = ordemServicoRepository.findByIdAndEmpresa_Id(dto.ordemServicoId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço com ID " + dto.ordemServicoId() + " não encontrada"));

        validarOrdemServicoParaAgendamento(ordemServico);

        Usuario tecnico = usuarioRepository.findByIdAndEmpresa_Id(dto.tecnicoId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Técnico com ID " + dto.tecnicoId() + " não encontrado"));

        validarTecnico(tecnico);

        validarIntervalo(dto.dataHoraInicio(), dto.dataHoraFim());

        validarSemSobreposicao(tecnico.getId(), empresaId, dto.dataHoraInicio(), dto.dataHoraFim(), null);

        Agendamento agendamento = new Agendamento(ordemServico, tecnico, dto.dataHoraInicio(), dto.dataHoraFim(), dto.observacao(), empresa);

        Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);

        registrarHistoricoStatus(agendamentoSalvo, null, StatusAgendamento.AGENDADO);

        return converterParaResponse(agendamentoSalvo);
    }

    private void validarOrdemServicoParaAgendamento(OrdemServico ordemServico) {

        if (ordemServico.getStatus() == StatusOrdemServico.CANCELADA) {

            throw new BusinessRuleException("Não é possível agendar uma ordem de serviço cancelada");
        }

        if (ordemServico.getStatus() == StatusOrdemServico.CONCLUIDA) {

            throw new BusinessRuleException("Não é possível agendar uma ordem de serviço concluída");
        }
    }

    private void validarTecnico(Usuario tecnico) {

        if (tecnico.getRole() != RoleUsuario.TECNICO) {

            throw new BusinessRuleException("O usuário informado não possui perfil de técnico");
        }

        if (!Boolean.TRUE.equals(tecnico.getAtivo())) {

            throw new BusinessRuleException("O técnico informado está inativo");
        }
    }

    private void validarIntervalo(LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim) {

        if (!dataHoraFim.isAfter(dataHoraInicio)) {

            throw new BusinessRuleException("A data/hora de fim deve ser posterior à data/hora de início");
        }
    }

    private void validarSemSobreposicao(Long tecnicoId, Long empresaId, LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, Long agendamentoIdExcluido) {

        List<Agendamento> conflitantes = agendamentoRepository.buscarConflitantes(tecnicoId, empresaId, STATUS_ATIVOS, dataHoraInicio, dataHoraFim, agendamentoIdExcluido);

        if (!conflitantes.isEmpty()) {

            throw new BusinessRuleException("O técnico já possui um agendamento nesse intervalo de horário");
        }
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<AgendamentoResponseDTO> listar(LocalDateTime dataInicial, LocalDateTime dataFinal, Long tecnicoId, StatusAgendamento status, int page, int size) {

        Long empresaId = obterEmpresaIdAtual();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dataHoraInicio"));

        Page<AgendamentoResponseDTO> resultado = agendamentoRepository.findAll(AgendamentoSpecifications.comFiltros(empresaId, tecnicoId, status, dataInicial, dataFinal), pageable).map(this::converterParaResponse);

        return PageResponseDTO.from(resultado);
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDTO buscarPorId(Long id) {

        return converterParaResponse(buscarEntidadePorId(id));
    }

    /*
     * Usado pelo dashboard operacional: próximos agendamentos
     * ativos (exclui CANCELADO/CONCLUIDO) dentro da janela informada.
     */
    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarProximos(int diasLimite) {

        Long empresaId = obterEmpresaIdAtual();

        LocalDateTime agora = LocalDateTime.now();

        LocalDateTime limite = agora.plusDays(diasLimite);

        List<StatusAgendamento> statusExcluidos = List.of(StatusAgendamento.CANCELADO, StatusAgendamento.CONCLUIDO);

        return agendamentoRepository.findByEmpresa_IdAndDataHoraInicioBetweenAndStatusNotInOrderByDataHoraInicioAsc(empresaId, agora, limite, statusExcluidos, PageRequest.of(0, LIMITE_PROXIMOS_AGENDAMENTOS)).stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarPorOrdemServico(Long ordemServicoId) {

        Long empresaId = obterEmpresaIdAtual();

        ordemServicoRepository.findByIdAndEmpresa_Id(ordemServicoId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço com ID " + ordemServicoId + " não encontrada"));

        return agendamentoRepository.findByOrdemServico_IdAndEmpresa_IdOrderByDataHoraInicioAsc(ordemServicoId, empresaId).stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarPorTecnico(Long tecnicoId) {

        Long empresaId = obterEmpresaIdAtual();

        usuarioRepository.findByIdAndEmpresa_Id(tecnicoId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Técnico com ID " + tecnicoId + " não encontrado"));

        return agendamentoRepository.findByTecnico_IdAndEmpresa_IdOrderByDataHoraInicioAsc(tecnicoId, empresaId).stream().map(this::converterParaResponse).toList();
    }

    private void validarTransicaoStatus(StatusAgendamento atual, StatusAgendamento novo) {

        boolean transicaoValida = switch (atual) {

            case AGENDADO -> novo == StatusAgendamento.CONFIRMADO || novo == StatusAgendamento.CANCELADO;

            case CONFIRMADO -> novo == StatusAgendamento.EM_ATENDIMENTO || novo == StatusAgendamento.CANCELADO;

            case EM_ATENDIMENTO -> novo == StatusAgendamento.CONCLUIDO || novo == StatusAgendamento.CANCELADO;

            case CONCLUIDO, CANCELADO -> false;
        };

        if (!transicaoValida) {

            throw new BusinessRuleException("Transição de status inválida: " + atual + " -> " + novo);
        }
    }

    @Transactional
    public AgendamentoResponseDTO atualizarStatus(Long id, AtualizarStatusAgendamentoRequestDTO dto) {

        Agendamento agendamento = buscarEntidadePorId(id);

        StatusAgendamento statusAnterior = agendamento.getStatus();

        StatusAgendamento novoStatus = dto.status();

        validarTransicaoStatus(statusAnterior, novoStatus);

        agendamento.setStatus(novoStatus);

        Agendamento agendamentoAtualizado = agendamentoRepository.save(agendamento);

        registrarHistoricoStatus(agendamentoAtualizado, statusAnterior, novoStatus);

        return converterParaResponse(agendamentoAtualizado);
    }

    @Transactional
    public AgendamentoResponseDTO reagendar(Long id, AgendamentoReagendarRequestDTO dto) {

        Agendamento agendamento = buscarEntidadePorId(id);

        validarAgendamentoNaoFinalizado(agendamento);

        validarIntervalo(dto.dataHoraInicio(), dto.dataHoraFim());

        validarSemSobreposicao(agendamento.getTecnico().getId(), agendamento.getEmpresa().getId(), dto.dataHoraInicio(), dto.dataHoraFim(), agendamento.getId());

        agendamento.setDataHoraInicio(dto.dataHoraInicio());

        agendamento.setDataHoraFim(dto.dataHoraFim());

        Agendamento agendamentoAtualizado = agendamentoRepository.save(agendamento);

        return converterParaResponse(agendamentoAtualizado);
    }

    private void validarAgendamentoNaoFinalizado(Agendamento agendamento) {

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO || agendamento.getStatus() == StatusAgendamento.CANCELADO) {

            throw new BusinessRuleException("Não é possível reagendar um agendamento concluído ou cancelado");
        }
    }

    @Transactional(readOnly = true)
    public List<AgendamentoHistoricoResponseDTO> listarHistorico(Long agendamentoId) {

        buscarEntidadePorId(agendamentoId);

        return historicoRepository.findByAgendamentoIdOrderByDataAlteracaoAsc(agendamentoId).stream().map(this::converterHistoricoParaResponse).toList();
    }

    private void registrarHistoricoStatus(Agendamento agendamento, StatusAgendamento statusAnterior, StatusAgendamento statusNovo) {

        Usuario usuarioAtual = usuarioAutenticadoService.obterUsuarioAtual();

        AgendamentoHistorico historico = new AgendamentoHistorico(agendamento, statusAnterior, statusNovo, usuarioAtual);

        historicoRepository.save(historico);
    }

    private Agendamento buscarEntidadePorId(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        return agendamentoRepository.findByIdAndEmpresa_Id(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Agendamento com ID " + id + " não encontrado"));
    }

    private Long obterEmpresaIdAtual() {

        return usuarioAutenticadoService.obterEmpresaAtual().getId();
    }

    private AgendamentoResponseDTO converterParaResponse(Agendamento agendamento) {

        return new AgendamentoResponseDTO(agendamento.getId(), agendamento.getOrdemServico().getId(), agendamento.getTecnico().getId(), agendamento.getTecnico().getNome(), agendamento.getDataHoraInicio(), agendamento.getDataHoraFim(), agendamento.getStatus(), agendamento.getObservacao(), agendamento.getDataCriacao());
    }

    private AgendamentoHistoricoResponseDTO converterHistoricoParaResponse(AgendamentoHistorico historico) {

        return new AgendamentoHistoricoResponseDTO(historico.getId(), historico.getStatusAnterior(), historico.getStatusNovo(), historico.getDataAlteracao(), historico.getUsuario() != null ? historico.getUsuario().getId() : null, historico.getUsuario() != null ? historico.getUsuario().getNome() : null);
    }
}
