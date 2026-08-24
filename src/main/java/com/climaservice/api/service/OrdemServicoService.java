package com.climaservice.api.service;

import com.climaservice.api.dto.*;
import com.climaservice.api.entity.*;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.ClienteRepository;
import com.climaservice.api.repository.EquipamentoRepository;
import com.climaservice.api.repository.OrdemServicoHistoricoRepository;
import com.climaservice.api.repository.OrdemServicoRepository;
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

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository, ClienteRepository clienteRepository, EquipamentoRepository equipamentoRepository, OrdemServicoHistoricoRepository historicoRepository, UsuarioAutenticadoService usuarioAutenticadoService) {

        this.ordemServicoRepository = ordemServicoRepository;
        this.clienteRepository = clienteRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.historicoRepository = historicoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public OrdemServicoResponseDTO salvar(OrdemServicoRequestDTO dto) {

        Cliente cliente = clienteRepository.findById(dto.clienteId()).orElseThrow(() -> new ResourceNotFoundException("Cliente com ID " + dto.clienteId() + " não encontrado"));

        Equipamento equipamento = equipamentoRepository.findById(dto.equipamentoId()).orElseThrow(() -> new ResourceNotFoundException("Equipamento com ID " + dto.equipamentoId() + " não encontrado"));

        validarEquipamentoDoCliente(cliente, equipamento);

        validarEquipamentoAtivo(equipamento);

        OrdemServico ordemServico = new OrdemServico(cliente, equipamento, dto.descricaoProblema());

        OrdemServico ordemServicoSalva = ordemServicoRepository.save(ordemServico);

        Usuario usuarioAtual = usuarioAutenticadoService.obterUsuarioAtual();

        OrdemServicoHistorico historico = new OrdemServicoHistorico(ordemServicoSalva, null, StatusOrdemServico.ABERTA, usuarioAtual);

        historicoRepository.save(historico);

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
    public List<OrdemServicoResponseDTO> listarTodas() {

        return ordemServicoRepository.findAll().stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<OrdemServicoResponseDTO> buscarPorId(Long id) {

        return ordemServicoRepository.findById(id).map(this::converterParaResponse);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarPorCliente(Long clienteId) {

        return ordemServicoRepository.findByClienteId(clienteId).stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarPorEquipamento(Long equipamentoId) {

        return ordemServicoRepository.findByEquipamentoId(equipamentoId).stream().map(this::converterParaResponse).toList();
    }

    @Transactional
    public OrdemServicoResponseDTO atualizarDiagnostico(Long id, AtualizarDiagnosticoRequestDTO dto) {

        OrdemServico ordemServico = ordemServicoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço com ID " + id + " não encontrada"));

        if (ordemServico.getStatus() == StatusOrdemServico.CANCELADA) {
            throw new BusinessRuleException("Não é possível alterar o diagnóstico de uma ordem de serviço cancelada");
        }

        if (ordemServico.getStatus() == StatusOrdemServico.CONCLUIDA) {
            throw new BusinessRuleException("Não é possível alterar o diagnóstico de uma ordem de serviço concluída");
        }

        ordemServico.setDiagnostico(dto.diagnostico());

        OrdemServico ordemServicoAtualizada = ordemServicoRepository.save(ordemServico);

        return converterParaResponse(ordemServicoAtualizada);
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

        OrdemServico ordemServico = ordemServicoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço com ID " + id + " não encontrada"));

        StatusOrdemServico novoStatus = dto.status();

        StatusOrdemServico statusAnterior = ordemServico.getStatus();

        validarTransicaoStatus(statusAnterior, novoStatus);

        ordemServico.setStatus(novoStatus);

        if (novoStatus == StatusOrdemServico.CONCLUIDA) {
            ordemServico.setDataConclusao(LocalDateTime.now());
        }

        OrdemServico ordemServicoAtualizada = ordemServicoRepository.save(ordemServico);

        Usuario usuarioAtual = usuarioAutenticadoService.obterUsuarioAtual();

        OrdemServicoHistorico historico = new OrdemServicoHistorico(ordemServico, statusAnterior, novoStatus, usuarioAtual);

        historicoRepository.save(historico);

        return converterParaResponse(ordemServicoAtualizada);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoHistoricoResponseDTO> listarHistorico(Long ordemServicoId) {

        if (!ordemServicoRepository.existsById(ordemServicoId)) {
            throw new ResourceNotFoundException("Ordem de serviço com ID " + ordemServicoId + " não encontrada");
        }

        return historicoRepository.findByOrdemServicoIdOrderByDataAlteracaoAsc(ordemServicoId).stream().map(this::converterHistoricoParaResponse).toList();
    }

    private OrdemServicoHistoricoResponseDTO converterHistoricoParaResponse(OrdemServicoHistorico historico) {

        return new OrdemServicoHistoricoResponseDTO(historico.getId(), historico.getStatusAnterior(), historico.getStatusNovo(), historico.getDataAlteracao(), historico.getUsuario() != null ? historico.getUsuario().getId() : null, historico.getUsuario() != null ? historico.getUsuario().getNome() : null);
    }

    private OrdemServicoResponseDTO converterParaResponse(OrdemServico ordemServico) {

        return new OrdemServicoResponseDTO(ordemServico.getId(),

                ordemServico.getCliente().getId(), ordemServico.getCliente().getNome(),

                ordemServico.getEquipamento().getId(), ordemServico.getEquipamento().getMarca(), ordemServico.getEquipamento().getModelo(),

                ordemServico.getDescricaoProblema(), ordemServico.getDiagnostico(),

                ordemServico.getStatus(),

                ordemServico.getDataAbertura(), ordemServico.getDataConclusao());
    }


}