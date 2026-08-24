package com.climaservice.api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ordem_servico_historico")
public class OrdemServicoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private StatusOrdemServico statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 30)
    private StatusOrdemServico statusNovo;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public OrdemServicoHistorico() {
    }

    public OrdemServicoHistorico(OrdemServico ordemServico, StatusOrdemServico statusAnterior, StatusOrdemServico statusNovo, Usuario usuario) {

        this.ordemServico = ordemServico;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.usuario = usuario;
        this.dataAlteracao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public StatusOrdemServico getStatusAnterior() {
        return statusAnterior;
    }

    public StatusOrdemServico getStatusNovo() {
        return statusNovo;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}