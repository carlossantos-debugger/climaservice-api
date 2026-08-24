package com.climaservice.api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ordem_servico_diagnostico_historico")
public class OrdemServicoDiagnosticoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Column(name = "diagnostico_anterior", length = 2000)
    private String diagnosticoAnterior;

    @Column(name = "diagnostico_novo", nullable = false, length = 2000)
    private String diagnosticoNovo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    public OrdemServicoDiagnosticoHistorico() {
    }

    public OrdemServicoDiagnosticoHistorico(
            OrdemServico ordemServico,
            String diagnosticoAnterior,
            String diagnosticoNovo,
            Usuario usuario) {

        this.ordemServico = ordemServico;
        this.diagnosticoAnterior = diagnosticoAnterior;
        this.diagnosticoNovo = diagnosticoNovo;
        this.usuario = usuario;
        this.dataAlteracao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public String getDiagnosticoAnterior() {
        return diagnosticoAnterior;
    }

    public String getDiagnosticoNovo() {
        return diagnosticoNovo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }
}