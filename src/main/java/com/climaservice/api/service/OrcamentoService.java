package com.climaservice.api.service;

import com.climaservice.api.dto.OrcamentoItemRequestDTO;
import com.climaservice.api.dto.OrcamentoItemResponseDTO;
import com.climaservice.api.dto.OrcamentoRequestDTO;
import com.climaservice.api.dto.OrcamentoResponseDTO;
import com.climaservice.api.entity.*;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.OrcamentoItemRepository;
import com.climaservice.api.repository.OrcamentoRepository;
import com.climaservice.api.repository.OrdemServicoRepository;
import com.climaservice.api.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoItemRepository orcamentoItemRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final ServicoRepository servicoRepository;

    public OrcamentoService(OrcamentoRepository orcamentoRepository, OrcamentoItemRepository orcamentoItemRepository, OrdemServicoRepository ordemServicoRepository, ServicoRepository servicoRepository) {

        this.orcamentoRepository = orcamentoRepository;
        this.orcamentoItemRepository = orcamentoItemRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public OrcamentoResponseDTO criar(Long ordemServicoId, OrcamentoRequestDTO dto) {

        OrdemServico ordemServico = ordemServicoRepository.findById(ordemServicoId).orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço com ID " + ordemServicoId + " não encontrada"));

        validarOrdemServicoParaOrcamento(ordemServico);

        Orcamento orcamento = new Orcamento(ordemServico, dto.observacao());

        Orcamento orcamentoSalvo = orcamentoRepository.save(orcamento);

        return converterParaResponse(orcamentoSalvo);
    }

    private void validarOrdemServicoParaOrcamento(OrdemServico ordemServico) {

        if (ordemServico.getStatus() == StatusOrdemServico.CANCELADA) {
            throw new BusinessRuleException("Não é possível criar orçamento para uma ordem de serviço cancelada");
        }

        if (ordemServico.getStatus() == StatusOrdemServico.CONCLUIDA) {
            throw new BusinessRuleException("Não é possível criar orçamento para uma ordem de serviço concluída");
        }
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponseDTO> listarPorOrdemServico(Long ordemServicoId) {

        if (!ordemServicoRepository.existsById(ordemServicoId)) {
            throw new ResourceNotFoundException("Ordem de serviço com ID " + ordemServicoId + " não encontrada");
        }

        return orcamentoRepository.findByOrdemServicoIdOrderByDataCriacaoDesc(ordemServicoId).stream().map(this::converterParaResponse).toList();
    }

    private Orcamento buscarOrcamentoPorId(Long id) {

        return orcamentoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Orçamento com ID " + id + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public OrcamentoResponseDTO buscarPorId(Long id) {

        Orcamento orcamento = buscarOrcamentoPorId(id);

        return converterParaResponse(orcamento);
    }

    @Transactional
    public OrcamentoItemResponseDTO adicionarItem(Long orcamentoId, OrcamentoItemRequestDTO dto) {

        Orcamento orcamento = buscarOrcamentoPorId(orcamentoId);

        validarOrcamentoRascunho(orcamento);

        Servico servico = servicoRepository.findById(dto.servicoId()).orElseThrow(() -> new ResourceNotFoundException("Serviço com ID " + dto.servicoId() + " não encontrado"));

        validarServicoAtivo(servico);

        BigDecimal valorUnitario = definirValorUnitario(dto, servico);

        OrcamentoItem item = new OrcamentoItem(orcamento, TipoItemOrcamento.SERVICO, servico, servico.getNome(), dto.quantidade(), valorUnitario);

        OrcamentoItem itemSalvo = orcamentoItemRepository.save(item);

        recalcularValorTotal(orcamento);

        return converterItemParaResponse(itemSalvo);
    }

    private void validarOrcamentoRascunho(Orcamento orcamento) {

        if (orcamento.getStatus() != StatusOrcamento.RASCUNHO) {
            throw new BusinessRuleException("Somente orçamentos em rascunho podem ser alterados");
        }
    }

    private void validarServicoAtivo(Servico servico) {

        if (!Boolean.TRUE.equals(servico.getAtivo())) {
            throw new BusinessRuleException("Não é possível adicionar um serviço inativo ao orçamento");
        }
    }

    private void recalcularValorTotal(Orcamento orcamento) {

        List<OrcamentoItem> itens = orcamentoItemRepository.findByOrcamentoIdOrderByIdAsc(orcamento.getId());

        BigDecimal total = itens.stream().map(OrcamentoItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        orcamento.setValorTotal(total);

        orcamentoRepository.save(orcamento);
    }

    @Transactional(readOnly = true)
    public List<OrcamentoItemResponseDTO> listarItens(Long orcamentoId) {

        buscarOrcamentoPorId(orcamentoId);

        return orcamentoItemRepository.findByOrcamentoIdOrderByIdAsc(orcamentoId).stream().map(this::converterItemParaResponse).toList();
    }

    private OrcamentoItemResponseDTO converterItemParaResponse(OrcamentoItem item) {

        return new OrcamentoItemResponseDTO(item.getId(), item.getTipo(), item.getServico() != null ? item.getServico().getId() : null, item.getDescricao(), item.getQuantidade(), item.getValorUnitario(), item.getSubtotal());
    }

    private BigDecimal definirValorUnitario(OrcamentoItemRequestDTO dto, Servico servico) {

        if (dto.valorUnitario() != null) {
            return dto.valorUnitario();
        }

        return servico.getValorPadrao();
    }

    private OrcamentoResponseDTO converterParaResponse(Orcamento orcamento) {

        return new OrcamentoResponseDTO(orcamento.getId(), orcamento.getOrdemServico().getId(), orcamento.getStatus(), orcamento.getValorTotal(), orcamento.getDataCriacao(), orcamento.getDataEnvio(), orcamento.getDataResposta(), orcamento.getObservacao());
    }
}