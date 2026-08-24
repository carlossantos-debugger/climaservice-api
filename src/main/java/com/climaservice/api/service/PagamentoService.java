package com.climaservice.api.service;

import com.climaservice.api.dto.PagamentoRequestDTO;
import com.climaservice.api.dto.PagamentoResponseDTO;
import com.climaservice.api.dto.PagamentoResumoResponseDTO;
import com.climaservice.api.entity.*;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.OrcamentoRepository;
import com.climaservice.api.repository.PagamentoHistoricoRepository;
import com.climaservice.api.repository.PagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.climaservice.api.dto.PagamentoHistoricoResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final PagamentoHistoricoRepository historicoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public PagamentoService(PagamentoRepository pagamentoRepository, OrcamentoRepository orcamentoRepository, PagamentoHistoricoRepository historicoRepository, UsuarioAutenticadoService usuarioAutenticadoService) {

        this.pagamentoRepository = pagamentoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.historicoRepository = historicoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public PagamentoResponseDTO criar(Long orcamentoId, PagamentoRequestDTO dto) {

        Orcamento orcamento = buscarOrcamentoPorId(orcamentoId);

        validarOrcamentoAprovado(orcamento);

        validarValorDisponivel(orcamento, dto.valor());

        Pagamento pagamento = new Pagamento(orcamento, dto.valor(), dto.formaPagamento(), dto.observacao());

        Pagamento pagamentoSalvo = pagamentoRepository.save(pagamento);

        registrarHistoricoStatus(pagamentoSalvo, null, StatusPagamento.PENDENTE);

        return converterParaResponse(pagamentoSalvo);
    }

    private Orcamento buscarOrcamentoPorId(Long id) {

        return orcamentoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Orçamento com ID " + id + " não encontrado"));
    }

    private void validarOrcamentoAprovado(Orcamento orcamento) {

        if (orcamento.getStatus() != StatusOrcamento.APROVADO) {
            throw new BusinessRuleException("Somente orçamentos aprovados podem receber pagamentos");
        }
    }

    private BigDecimal calcularTotalPorStatus(Long orcamentoId, StatusPagamento status) {

        return pagamentoRepository.findByOrcamentoIdAndStatus(orcamentoId, status).stream().map(Pagamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validarValorDisponivel(Orcamento orcamento, BigDecimal novoValor) {

        BigDecimal totalConfirmado = calcularTotalPorStatus(orcamento.getId(), StatusPagamento.CONFIRMADO);

        BigDecimal totalPendente = calcularTotalPorStatus(orcamento.getId(), StatusPagamento.PENDENTE);

        BigDecimal valorComprometido = totalConfirmado.add(totalPendente);

        BigDecimal valorDisponivel = orcamento.getValorTotal().subtract(valorComprometido);

        if (novoValor.compareTo(valorDisponivel) > 0) {
            throw new BusinessRuleException("O valor do pagamento ultrapassa o saldo disponível de " + valorDisponivel);
        }
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarPorOrcamento(Long orcamentoId) {

        buscarOrcamentoPorId(orcamentoId);

        return pagamentoRepository.findByOrcamentoIdOrderByDataCriacaoAsc(orcamentoId).stream().map(this::converterParaResponse).toList();
    }

    private Pagamento buscarPagamentoPorId(Long id) {

        return pagamentoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pagamento com ID " + id + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public PagamentoResponseDTO buscarPorId(Long id) {

        return converterParaResponse(buscarPagamentoPorId(id));
    }

    @Transactional
    public PagamentoResponseDTO confirmar(Long id) {

        Pagamento pagamento = buscarPagamentoPorId(id);

        validarPagamentoPendente(pagamento);

        StatusPagamento statusAnterior = pagamento.getStatus();

        pagamento.setStatus(StatusPagamento.CONFIRMADO);

        pagamento.setDataConfirmacao(LocalDateTime.now());

        Pagamento pagamentoAtualizado = pagamentoRepository.save(pagamento);

        registrarHistoricoStatus(pagamentoAtualizado, statusAnterior, StatusPagamento.CONFIRMADO);

        return converterParaResponse(pagamentoAtualizado);
    }

    @Transactional
    public PagamentoResponseDTO cancelar(Long id) {

        Pagamento pagamento = buscarPagamentoPorId(id);

        validarPagamentoPendente(pagamento);

        StatusPagamento statusAnterior = pagamento.getStatus();

        pagamento.setStatus(StatusPagamento.CANCELADO);

        pagamento.setDataCancelamento(LocalDateTime.now());

        Pagamento pagamentoAtualizado = pagamentoRepository.save(pagamento);

        registrarHistoricoStatus(pagamentoAtualizado, statusAnterior, StatusPagamento.CANCELADO);

        return converterParaResponse(pagamentoAtualizado);
    }

    private void validarPagamentoPendente(Pagamento pagamento) {

        if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
            throw new BusinessRuleException("Somente pagamentos pendentes podem ser alterados");
        }
    }

    @Transactional(readOnly = true)
    public PagamentoResumoResponseDTO obterResumo(Long orcamentoId) {

        Orcamento orcamento = buscarOrcamentoPorId(orcamentoId);

        BigDecimal totalPago = calcularTotalPorStatus(orcamentoId, StatusPagamento.CONFIRMADO);

        BigDecimal totalPendente = calcularTotalPorStatus(orcamentoId, StatusPagamento.PENDENTE);

        BigDecimal saldoRestante = orcamento.getValorTotal().subtract(totalPago);

        BigDecimal valorDisponivelParaNovoPagamento = saldoRestante.subtract(totalPendente);

        return new PagamentoResumoResponseDTO(orcamento.getId(), orcamento.getValorTotal(), totalPago, totalPendente, saldoRestante, valorDisponivelParaNovoPagamento);
    }

    private void registrarHistoricoStatus(Pagamento pagamento, StatusPagamento statusAnterior, StatusPagamento statusNovo) {

        Usuario usuarioAtual = usuarioAutenticadoService.obterUsuarioAtual();

        PagamentoHistorico historico = new PagamentoHistorico(pagamento, statusAnterior, statusNovo, usuarioAtual);

        historicoRepository.save(historico);
    }

    @Transactional(readOnly = true)
    public List<PagamentoHistoricoResponseDTO> listarHistorico(Long pagamentoId) {

        buscarPagamentoPorId(pagamentoId);

        return historicoRepository.findByPagamentoIdOrderByDataAlteracaoAsc(pagamentoId).stream().map(this::converterHistoricoParaResponse).toList();
    }

    private PagamentoHistoricoResponseDTO converterHistoricoParaResponse(PagamentoHistorico historico) {

        return new PagamentoHistoricoResponseDTO(historico.getId(), historico.getStatusAnterior(), historico.getStatusNovo(), historico.getDataAlteracao(),

                historico.getUsuario() != null ? historico.getUsuario().getId() : null,

                historico.getUsuario() != null ? historico.getUsuario().getNome() : null);
    }

    private PagamentoResponseDTO converterParaResponse(Pagamento pagamento) {

        return new PagamentoResponseDTO(pagamento.getId(), pagamento.getOrcamento().getId(), pagamento.getValor(), pagamento.getFormaPagamento(), pagamento.getStatus(), pagamento.getDataCriacao(), pagamento.getDataConfirmacao(), pagamento.getDataCancelamento(), pagamento.getObservacao());
    }
}