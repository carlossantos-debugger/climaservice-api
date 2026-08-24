package com.climaservice.api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orcamento_historico")
public class OrcamentoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orcamento_id", nullable = false)
    private Orcamento orcamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 20)
    private StatusOrcamento statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 20)
    private StatusOrcamento statusNovo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    public OrcamentoHistorico() {
    }

    public OrcamentoHistorico(
            Orcamento orcamento,
            StatusOrcamento statusAnterior,
            StatusOrcamento statusNovo,
            Usuario usuario) {

        this.orcamento = orcamento;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.usuario = usuario;
        this.dataAlteracao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public StatusOrcamento getStatusAnterior() {
        return statusAnterior;
    }

    public StatusOrcamento getStatusNovo() {
        return statusNovo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }
}