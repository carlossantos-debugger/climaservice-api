package com.climaservice.api.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orcamento_id", nullable = false)
    private Orcamento orcamento;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;

    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;

    @Column(length = 500)
    private String observacao;

    public Pagamento() {
    }

    public Pagamento(Orcamento orcamento, BigDecimal valor, FormaPagamento formaPagamento, String observacao) {
        this.orcamento = orcamento;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
        this.observacao = observacao;

        this.status = StatusPagamento.PENDENTE;

        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataConfirmacao() {
        return dataConfirmacao;
    }

    public void setDataConfirmacao(LocalDateTime dataConfirmacao) {
        this.dataConfirmacao = dataConfirmacao;
    }

    public LocalDateTime getDataCancelamento() {
        return dataCancelamento;
    }

    public void setDataCancelamento(LocalDateTime dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
    }

    public String getObservacao() {
        return observacao;
    }
}