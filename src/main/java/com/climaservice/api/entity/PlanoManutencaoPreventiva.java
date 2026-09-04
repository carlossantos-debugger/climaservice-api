package com.climaservice.api.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plano_manutencao_preventiva")
public class PlanoManutencaoPreventiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipamento_id", nullable = false)
    private Equipamento equipamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_padrao_id")
    private Usuario tecnicoPadrao;

    @Column(name = "intervalo_meses", nullable = false)
    private Integer intervaloMeses;

    @Column(name = "proxima_execucao", nullable = false)
    private LocalDate proximaExecucao;

    @Column(name = "ultima_execucao")
    private LocalDate ultimaExecucao;

    @Column(nullable = false)
    private Boolean ativo;

    @Column(length = 1000)
    private String observacao;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    public PlanoManutencaoPreventiva() {
    }

    public PlanoManutencaoPreventiva(Equipamento equipamento, Usuario tecnicoPadrao, Integer intervaloMeses, LocalDate proximaExecucao, String observacao, Empresa empresa) {

        this.equipamento = equipamento;
        this.tecnicoPadrao = tecnicoPadrao;
        this.intervaloMeses = intervaloMeses;
        this.proximaExecucao = proximaExecucao;
        this.observacao = observacao;
        this.ativo = true;
        this.dataCriacao = LocalDateTime.now();
        this.empresa = empresa;
    }

    public Long getId() {
        return id;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public Usuario getTecnicoPadrao() {
        return tecnicoPadrao;
    }

    public void setTecnicoPadrao(Usuario tecnicoPadrao) {
        this.tecnicoPadrao = tecnicoPadrao;
    }

    public Integer getIntervaloMeses() {
        return intervaloMeses;
    }

    public void setIntervaloMeses(Integer intervaloMeses) {
        this.intervaloMeses = intervaloMeses;
    }

    public LocalDate getProximaExecucao() {
        return proximaExecucao;
    }

    public void setProximaExecucao(LocalDate proximaExecucao) {
        this.proximaExecucao = proximaExecucao;
    }

    public LocalDate getUltimaExecucao() {
        return ultimaExecucao;
    }

    public void setUltimaExecucao(LocalDate ultimaExecucao) {
        this.ultimaExecucao = ultimaExecucao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public Empresa getEmpresa() {
        return empresa;
    }
}
