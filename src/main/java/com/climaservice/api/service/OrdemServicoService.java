package com.climaservice.api.service;

import com.climaservice.api.dto.*;
import com.climaservice.api.entity.*;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final OrdemServicoHistoricoRepository historicoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final OrdemServicoDiagnosticoHistoricoRepository diagnosticoHistoricoRepository;

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository, ClienteRepository clienteRepository, EquipamentoRepository equipamentoRepository, OrdemServicoHistoricoRepository historicoRepository, UsuarioAutenticadoService usuarioAutenticadoService, OrdemServicoDiagnosticoHistoricoRepository diagnosticoHistoricoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.clienteRepository = clienteRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.historicoRepository = historicoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.diagnosticoHistoricoRepository = diagnosticoHistoricoRepository;
    }

    @Transactional
    public OrdemServicoResponseDTO salvar(OrdemServicoRequestDTO dto) {

        Empresa empresa = usuarioAutenticadoService.obterEmpresaAtual();

        Long empresaId = empresa.getId();

        Cliente cliente = clienteRepository.findByIdAndEmpresa_Id(dto.clienteId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Cliente com ID " + dto.clienteId() + " não encontrado"));

        Equipamento equipamento = equipamentoRepository.findByIdAndEmpresa_Id(dto.equipamentoId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Equipamento com ID " + dto.equipamentoId() + " não encontrado"));

        validarEquipamentoDoCliente(cliente, equipamento);

        validarEquipamentoAtivo(equipamento);

        OrdemServico ordemServico = new OrdemServico(cliente, equipamento, dto.descricaoProblema(), empresa);

        OrdemServico ordemServicoSalva = ordemServicoRepository.save(ordemServico);

        registrarHistoricoStatus(ordemServicoSalva, null, StatusOrdemServico.ABERTA);

        return converterParaResponse(ordemServicoSalva);
    }

    private void validarEquipamentoDoCliente(Cliente cliente, Equipamento equipamento) {

        if (!equipamento.getCliente().getId().equals(cliente.getId())) {

            throw new BusinessRuleException("O equipamento informado não pertence ao cliente");
        }
    }

    private void validarEquipamentoAtivo(Equipamento equipamento) {

        if (equipamento.getStatus() != StatusEquipamento.ATIVO) {

            throw new BusinessRuleException("Não é possível abrir uma ordem de serviço " + "para um equipamento inativo");
        }
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<OrdemServicoResponseDTO> listar(StatusOrdemServico status, Long clienteId, Long equipamentoId, LocalDateTime dataInicial, LocalDateTime dataFinal, int page, int size) {

        Long empresaId = obterEmpresaIdAtual();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataAbertura"));

        Page<OrdemServicoResponseDTO> resultado = ordemServicoRepository.findAll(OrdemServicoSpecifications.comFiltros(empresaId, status, clienteId, equipamentoId, dataInicial, dataFinal), pageable).map(this::converterParaResponse);

        return PageResponseDTO.from(resultado);
    }

    @Transactional(readOnly = true)
    public Optional<OrdemServicoResponseDTO> buscarPorId(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        return ordemServicoRepository.findByIdAndEmpresa_Id(id, empresaId).map(this::converterParaResponse);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarPorCliente(Long clienteId) {

        Long empresaId = obterEmpresaIdAtual();

        /*
         * Impede que alguém consulte ordens usando
         * o ID de um cliente de outro tenant.
         */
        buscarClienteDaEmpresaAtual(clienteId, empresaId);

        return ordemServicoRepository.findByCliente_IdAndEmpresa_IdOrderByDataAberturaDesc(clienteId, empresaId).stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarPorEquipamento(Long equipamentoId) {

        Long empresaId = obterEmpresaIdAtual();

        buscarEquipamentoDaEmpresaAtual(equipamentoId, empresaId);

        return ordemServicoRepository.findByEquipamento_IdAndEmpresa_IdOrderByDataAberturaDesc(equipamentoId, empresaId).stream().map(this::converterParaResponse).toList();
    }

    @Transactional
    public OrdemServicoResponseDTO atualizarDiagnostico(Long id, AtualizarDiagnosticoRequestDTO dto) {

        OrdemServico ordemServico = buscarEntidadePorId(id);

        if (ordemServico.getStatus() == StatusOrdemServico.CONCLUIDA || ordemServico.getStatus() == StatusOrdemServico.CANCELADA) {

            throw new BusinessRuleException("Não é possível alterar o diagnóstico " + "de uma ordem concluída ou cancelada");
        }

        String diagnosticoAnterior = ordemServico.getDiagnostico();

        ordemServico.setDiagnostico(dto.diagnostico());

        OrdemServico ordemAtualizada = ordemServicoRepository.save(ordemServico);

        registrarHistoricoDiagnostico(ordemAtualizada, diagnosticoAnterior, dto.diagnostico());

        return converterParaResponse(ordemAtualizada);
    }

    private void validarTransicaoStatus(StatusOrdemServico atual, StatusOrdemServico novo) {

        boolean transicaoValida = switch (atual) {

            case ABERTA -> novo == StatusOrdemServico.EM_ANDAMENTO || novo == StatusOrdemServico.CANCELADA;

            case EM_ANDAMENTO ->
                    novo == StatusOrdemServico.AGUARDANDO_CLIENTE || novo == StatusOrdemServico.CONCLUIDA || novo == StatusOrdemServico.CANCELADA;

            case AGUARDANDO_CLIENTE -> novo == StatusOrdemServico.EM_ANDAMENTO || novo == StatusOrdemServico.CANCELADA;

            case CONCLUIDA, CANCELADA -> false;
        };

        if (!transicaoValida) {

            throw new BusinessRuleException("Transição de status inválida: " + atual + " -> " + novo);
        }
    }

    @Transactional
    public OrdemServicoResponseDTO atualizarStatus(Long id, AtualizarStatusOrdemServicoRequestDTO dto) {

        OrdemServico ordemServico = buscarEntidadePorId(id);

        StatusOrdemServico novoStatus = dto.status();

        StatusOrdemServico statusAnterior = ordemServico.getStatus();

        validarTransicaoStatus(statusAnterior, novoStatus);

        ordemServico.setStatus(novoStatus);

        if (novoStatus == StatusOrdemServico.CONCLUIDA) {

            ordemServico.setDataConclusao(LocalDateTime.now());
        }

        OrdemServico ordemServicoAtualizada = ordemServicoRepository.save(ordemServico);

        registrarHistoricoStatus(ordemServicoAtualizada, statusAnterior, novoStatus);

        return converterParaResponse(ordemServicoAtualizada);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoHistoricoResponseDTO> listarHistorico(Long ordemServicoId) {

        /*
         * Primeiro verifica a OS dentro do tenant.
         *
         * Portanto uma OS de outra empresa será tratada
         * como inexistente.
         */
        buscarEntidadePorId(ordemServicoId);

        return historicoRepository.findByOrdemServicoIdOrderByDataAlteracaoAsc(ordemServicoId).stream().map(this::converterHistoricoParaResponse).toList();
    }

    private OrdemServicoHistoricoResponseDTO converterHistoricoParaResponse(OrdemServicoHistorico historico) {

        return new OrdemServicoHistoricoResponseDTO(historico.getId(), historico.getStatusAnterior(), historico.getStatusNovo(), historico.getDataAlteracao(), historico.getUsuario() != null ? historico.getUsuario().getId() : null, historico.getUsuario() != null ? historico.getUsuario().getNome() : null);
    }

    private OrdemServico buscarEntidadePorId(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        return ordemServicoRepository.findByIdAndEmpresa_Id(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço com ID " + id + " não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoDiagnosticoHistoricoResponseDTO> listarHistoricoDiagnostico(Long ordemServicoId) {

        /*
         * Mesma proteção aplicada ao histórico
         * de diagnóstico.
         */
        buscarEntidadePorId(ordemServicoId);

        return diagnosticoHistoricoRepository.findByOrdemServicoIdOrderByDataAlteracaoAsc(ordemServicoId).stream().map(this::converterDiagnosticoHistoricoParaResponse).toList();
    }

    private void registrarHistoricoStatus(OrdemServico ordemServico, StatusOrdemServico statusAnterior, StatusOrdemServico statusNovo) {

        Usuario usuarioAtual = usuarioAutenticadoService.obterUsuarioAtual();

        OrdemServicoHistorico historico = new OrdemServicoHistorico(ordemServico, statusAnterior, statusNovo, usuarioAtual);

        historicoRepository.save(historico);
    }

    private void registrarHistoricoDiagnostico(OrdemServico ordemServico, String diagnosticoAnterior, String diagnosticoNovo) {

        Usuario usuarioAtual = usuarioAutenticadoService.obterUsuarioAtual();

        OrdemServicoDiagnosticoHistorico historico = new OrdemServicoDiagnosticoHistorico(ordemServico, diagnosticoAnterior, diagnosticoNovo, usuarioAtual);

        diagnosticoHistoricoRepository.save(historico);
    }

    private Cliente buscarClienteDaEmpresaAtual(Long clienteId, Long empresaId) {

        return clienteRepository.findByIdAndEmpresa_Id(clienteId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Cliente com ID " + clienteId + " não encontrado"));
    }

    private Equipamento buscarEquipamentoDaEmpresaAtual(Long equipamentoId, Long empresaId) {

        return equipamentoRepository.findByIdAndEmpresa_Id(equipamentoId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Equipamento com ID " + equipamentoId + " não encontrado"));
    }

    private Long obterEmpresaIdAtual() {

        return usuarioAutenticadoService.obterEmpresaAtual().getId();
    }

    private OrdemServicoResponseDTO converterParaResponse(OrdemServico ordemServico) {

        return new OrdemServicoResponseDTO(ordemServico.getId(),

                ordemServico.getCliente().getId(), ordemServico.getCliente().getNome(),

                ordemServico.getEquipamento().getId(), ordemServico.getEquipamento().getMarca(), ordemServico.getEquipamento().getModelo(),

                ordemServico.getDescricaoProblema(), ordemServico.getDiagnostico(),

                ordemServico.getStatus(),

                ordemServico.getDataAbertura(), ordemServico.getDataConclusao());
    }

    private OrdemServicoDiagnosticoHistoricoResponseDTO converterDiagnosticoHistoricoParaResponse(OrdemServicoDiagnosticoHistorico historico) {

        return new OrdemServicoDiagnosticoHistoricoResponseDTO(historico.getId(), historico.getDiagnosticoAnterior(), historico.getDiagnosticoNovo(), historico.getDataAlteracao(), historico.getUsuario() != null ? historico.getUsuario().getId() : null, historico.getUsuario() != null ? historico.getUsuario().getNome() : null);
    }
}