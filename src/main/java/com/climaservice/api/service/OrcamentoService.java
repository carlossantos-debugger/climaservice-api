package com.climaservice.api.service;

import com.climaservice.api.dto.*;
import com.climaservice.api.entity.*;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoItemRepository orcamentoItemRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final ServicoRepository servicoRepository;
    private final ProdutoRepository produtoRepository;
    private final OrcamentoHistoricoRepository historicoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public OrcamentoService(OrcamentoRepository orcamentoRepository, OrcamentoItemRepository orcamentoItemRepository, OrdemServicoRepository ordemServicoRepository, ServicoRepository servicoRepository, ProdutoRepository produtoRepository, OrcamentoHistoricoRepository historicoRepository, UsuarioAutenticadoService usuarioAutenticadoService) {
        this.orcamentoRepository = orcamentoRepository;
        this.orcamentoItemRepository = orcamentoItemRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.servicoRepository = servicoRepository;
        this.produtoRepository = produtoRepository;
        this.historicoRepository = historicoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public OrcamentoResponseDTO criar(Long ordemServicoId, OrcamentoRequestDTO dto) {

        Empresa empresa = usuarioAutenticadoService.obterEmpresaAtual();

        Long empresaId = empresa.getId();

        OrdemServico ordemServico = ordemServicoRepository.findByIdAndEmpresa_Id(ordemServicoId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço com ID " + ordemServicoId + " não encontrada"));

        validarOrdemServicoParaOrcamento(ordemServico);

        Orcamento orcamento = new Orcamento(ordemServico, dto.observacao(), empresa);

        Orcamento orcamentoSalvo = orcamentoRepository.save(orcamento);

        registrarHistoricoStatus(orcamentoSalvo, null, StatusOrcamento.RASCUNHO);

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

        Long empresaId = obterEmpresaIdAtual();

        ordemServicoRepository.findByIdAndEmpresa_Id(ordemServicoId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço com ID " + ordemServicoId + " não encontrada"));

        return orcamentoRepository.findByOrdemServico_IdAndEmpresa_IdOrderByDataCriacaoDesc(ordemServicoId, empresaId).stream().map(this::converterParaResponse).toList();
    }

    private Orcamento buscarOrcamentoPorId(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        return orcamentoRepository.findByIdAndEmpresa_Id(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Orçamento com ID " + id + " não encontrado"));
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

        Long empresaId = obterEmpresaIdAtual();

        Servico servico = servicoRepository.findByIdAndEmpresa_Id(dto.servicoId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Serviço com ID " + dto.servicoId() + " não encontrado"));

        validarServicoAtivo(servico);

        BigDecimal valorUnitario = definirValorUnitario(dto, servico);

        OrcamentoItem item = new OrcamentoItem(orcamento, TipoItemOrcamento.SERVICO, servico, servico.getNome(), dto.quantidade(), valorUnitario);

        OrcamentoItem itemSalvo = orcamentoItemRepository.save(item);

        recalcularValorTotal(orcamento);

        return converterItemParaResponse(itemSalvo);
    }

    @Transactional
    public OrcamentoItemResponseDTO adicionarProduto(Long orcamentoId, OrcamentoProdutoItemRequestDTO dto) {

        Orcamento orcamento = buscarOrcamentoPorId(orcamentoId);

        validarOrcamentoRascunho(orcamento);

        Long empresaId = obterEmpresaIdAtual();

        Produto produto = produtoRepository.findByIdAndEmpresa_Id(dto.produtoId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + dto.produtoId() + " não encontrado"));

        validarProdutoAtivo(produto);

        BigDecimal valorUnitario = definirValorUnitarioProduto(dto, produto);

        OrcamentoItem item = new OrcamentoItem(orcamento, TipoItemOrcamento.PECA, produto, produto.getNome(), dto.quantidade(), valorUnitario);

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

    private void validarProdutoAtivo(Produto produto) {

        if (!Boolean.TRUE.equals(produto.getAtivo())) {

            throw new BusinessRuleException("Não é possível adicionar um produto inativo ao orçamento");
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

        return new OrcamentoItemResponseDTO(item.getId(), item.getTipo(),

                item.getServico() != null ? item.getServico().getId() : null,

                item.getProduto() != null ? item.getProduto().getId() : null,

                item.getDescricao(), item.getQuantidade(), item.getValorUnitario(), item.getSubtotal());
    }

    private BigDecimal definirValorUnitario(OrcamentoItemRequestDTO dto, Servico servico) {

        if (dto.valorUnitario() != null) {
            return dto.valorUnitario();
        }

        return servico.getValorPadrao();
    }

    private BigDecimal definirValorUnitarioProduto(OrcamentoProdutoItemRequestDTO dto, Produto produto) {

        if (dto.valorUnitario() != null) {
            return dto.valorUnitario();
        }

        return produto.getValorPadrao();
    }

    @Transactional
    public OrcamentoResponseDTO atualizarStatus(Long id, AtualizarStatusOrcamentoRequestDTO dto) {

        Orcamento orcamento = buscarOrcamentoPorId(id);

        StatusOrcamento statusAnterior = orcamento.getStatus();

        StatusOrcamento novoStatus = dto.status();

        validarTransicaoStatus(statusAnterior, novoStatus);

        if (novoStatus == StatusOrcamento.ENVIADO) {

            validarOrcamentoPossuiItens(orcamento);

            orcamento.setDataEnvio(LocalDateTime.now());
        }

        if (novoStatus == StatusOrcamento.APROVADO || novoStatus == StatusOrcamento.REJEITADO) {

            orcamento.setDataResposta(LocalDateTime.now());
        }

        orcamento.setStatus(novoStatus);

        Orcamento orcamentoAtualizado = orcamentoRepository.save(orcamento);

        registrarHistoricoStatus(orcamentoAtualizado, statusAnterior, novoStatus);

        return converterParaResponse(orcamentoAtualizado);
    }

    private void validarTransicaoStatus(StatusOrcamento atual, StatusOrcamento novo) {

        boolean transicaoValida = switch (atual) {

            case RASCUNHO -> novo == StatusOrcamento.ENVIADO || novo == StatusOrcamento.CANCELADO;

            case ENVIADO -> novo == StatusOrcamento.APROVADO || novo == StatusOrcamento.REJEITADO;

            case APROVADO, REJEITADO, CANCELADO -> false;
        };

        if (!transicaoValida) {

            throw new BusinessRuleException("Transição de status inválida: " + atual + " -> " + novo);
        }
    }

    private void validarOrcamentoPossuiItens(Orcamento orcamento) {

        List<OrcamentoItem> itens = orcamentoItemRepository.findByOrcamentoIdOrderByIdAsc(orcamento.getId());

        if (itens.isEmpty()) {

            throw new BusinessRuleException("Não é possível enviar um orçamento sem itens");
        }
    }

    @Transactional
    public OrcamentoItemResponseDTO atualizarItem(Long orcamentoId, Long itemId, AtualizarOrcamentoItemRequestDTO dto) {

        Orcamento orcamento = buscarOrcamentoPorId(orcamentoId);

        validarOrcamentoRascunho(orcamento);

        Long empresaId = obterEmpresaIdAtual();

        OrcamentoItem item = buscarItemDoOrcamento(itemId, orcamentoId, empresaId);

        BigDecimal valorUnitario = dto.valorUnitario() != null ? dto.valorUnitario() : item.getValorUnitario();

        item.atualizar(dto.quantidade(), valorUnitario);

        OrcamentoItem itemAtualizado = orcamentoItemRepository.save(item);

        recalcularValorTotal(orcamento);

        return converterItemParaResponse(itemAtualizado);
    }

    private OrcamentoItem buscarItemDoOrcamento(Long itemId, Long orcamentoId, Long empresaId) {

        return orcamentoItemRepository.findByIdAndOrcamento_IdAndOrcamento_Empresa_Id(itemId, orcamentoId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Item de orçamento com ID " + itemId + " não encontrado"));
    }

    @Transactional
    public void removerItem(Long orcamentoId, Long itemId) {

        Orcamento orcamento = buscarOrcamentoPorId(orcamentoId);

        validarOrcamentoRascunho(orcamento);

        Long empresaId = obterEmpresaIdAtual();

        OrcamentoItem item = buscarItemDoOrcamento(itemId, orcamentoId, empresaId);

        orcamentoItemRepository.delete(item);

        orcamentoItemRepository.flush();

        recalcularValorTotal(orcamento);
    }

    private void registrarHistoricoStatus(Orcamento orcamento, StatusOrcamento statusAnterior, StatusOrcamento statusNovo) {

        Usuario usuarioAtual = usuarioAutenticadoService.obterUsuarioAtual();

        OrcamentoHistorico historico = new OrcamentoHistorico(orcamento, statusAnterior, statusNovo, usuarioAtual);

        historicoRepository.save(historico);
    }

    @Transactional(readOnly = true)
    public List<OrcamentoHistoricoResponseDTO> listarHistorico(Long orcamentoId) {

        /*
         * Protege o histórico pelo tenant antes
         * de consultar OrcamentoHistorico.
         */
        buscarOrcamentoPorId(orcamentoId);

        return historicoRepository.findByOrcamentoIdOrderByDataAlteracaoAsc(orcamentoId).stream().map(this::converterHistoricoParaResponse).toList();
    }

    private OrcamentoHistoricoResponseDTO converterHistoricoParaResponse(OrcamentoHistorico historico) {

        return new OrcamentoHistoricoResponseDTO(historico.getId(), historico.getStatusAnterior(), historico.getStatusNovo(), historico.getDataAlteracao(),

                historico.getUsuario() != null ? historico.getUsuario().getId() : null,

                historico.getUsuario() != null ? historico.getUsuario().getNome() : null);
    }

    private Long obterEmpresaIdAtual() {

        return usuarioAutenticadoService.obterEmpresaAtual().getId();
    }

    private OrcamentoResponseDTO converterParaResponse(Orcamento orcamento) {

        return new OrcamentoResponseDTO(orcamento.getId(), orcamento.getOrdemServico().getId(), orcamento.getStatus(), orcamento.getValorTotal(), orcamento.getDataCriacao(), orcamento.getDataEnvio(), orcamento.getDataResposta(), orcamento.getObservacao());
    }
}