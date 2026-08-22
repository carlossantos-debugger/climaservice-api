package com.climaservice.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipamento")
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String marca;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(name = "capacidade_btu", nullable = false)
    private Integer capacidadeBtu;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "local_instalacao", length = 150)
    private String localInstalacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    public Equipamento() {
    }

    public Equipamento(String marca, String modelo, Integer capacidadeBtu, String numeroSerie, String localInstalacao, Cliente cliente) {

        this.marca = marca;
        this.modelo = modelo;
        this.capacidadeBtu = capacidadeBtu;
        this.numeroSerie = numeroSerie;
        this.localInstalacao = localInstalacao;
        this.cliente = cliente;
        this.status = StatusEquipamento.ATIVO;
    }

    public Long getId() {
        return id;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getCapacidadeBtu() {
        return capacidadeBtu;
    }

    public void setCapacidadeBtu(Integer capacidadeBtu) {
        this.capacidadeBtu = capacidadeBtu;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getLocalInstalacao() {
        return localInstalacao;
    }

    public void setLocalInstalacao(String localInstalacao) {
        this.localInstalacao = localInstalacao;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public StatusEquipamento getStatus() {
        return status;
    }

    public void setStatus(StatusEquipamento status) {
        this.status = status;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEquipamento status;


}