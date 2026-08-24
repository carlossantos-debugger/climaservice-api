package com.climaservice.api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento_historico")
public class PagamentoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pagamento_id", nullable = false)
    private Pagamento pagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 20)
    private StatusPagamento statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 20)
    private StatusPagamento statusNovo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    public PagamentoHistorico() {
    }

    public PagamentoHistorico(
            Pagamento pagamento,
            StatusPagamento statusAnterior,
            StatusPagamento statusNovo,
            Usuario usuario) {

        this.pagamento = pagamento;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.usuario = usuario;
        this.dataAlteracao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public StatusPagamento getStatusAnterior() {
        return statusAnterior;
    }

    public StatusPagamento getStatusNovo() {
        return statusNovo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }
}