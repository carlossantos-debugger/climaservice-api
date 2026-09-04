package com.climaservice.api.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plano_manutencao_preventiva_execucao")
public class PlanoManutencaoPreventivaExecucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plano_manutencao_preventiva_id", nullable = false)
    private PlanoManutencaoPreventiva plano;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Column(name = "data_execucao", nullable = false)
    private LocalDateTime dataExecucao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public PlanoManutencaoPreventivaExecucao() {
    }

    public PlanoManutencaoPreventivaExecucao(PlanoManutencaoPreventiva plano, OrdemServico ordemServico, LocalDate dataReferencia, Usuario usuario) {

        this.plano = plano;
        this.ordemServico = ordemServico;
        this.dataReferencia = dataReferencia;
        this.usuario = usuario;
        this.dataExecucao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public PlanoManutencaoPreventiva getPlano() {
        return plano;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public LocalDateTime getDataExecucao() {
        return dataExecucao;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
