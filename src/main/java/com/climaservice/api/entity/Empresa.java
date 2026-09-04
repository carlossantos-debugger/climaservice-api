package com.climaservice.api.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "cpf_cnpj", length = 14)
    private String cpfCnpj;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Embedded
    private Endereco endereco;

    @Column(name = "inscricao_municipal", length = 30)
    private String inscricaoMunicipal;

    @Enumerated(EnumType.STRING)
    @Column(name = "regime_tributario", length = 30)
    private RegimeTributario regimeTributario;

    @Column(name = "codigo_servico_padrao", length = 20)
    private String codigoServicoPadrao;

    @Column(name = "aliquota_iss_padrao", precision = 5, scale = 2)
    private BigDecimal aliquotaIssPadrao;

    protected Empresa() {
    }

    public Empresa(String nome, String cpfCnpj) {
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.ativo = true;
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getInscricaoMunicipal() {
        return inscricaoMunicipal;
    }

    public void setInscricaoMunicipal(String inscricaoMunicipal) {
        this.inscricaoMunicipal = inscricaoMunicipal;
    }

    public RegimeTributario getRegimeTributario() {
        return regimeTributario;
    }

    public void setRegimeTributario(RegimeTributario regimeTributario) {
        this.regimeTributario = regimeTributario;
    }

    public String getCodigoServicoPadrao() {
        return codigoServicoPadrao;
    }

    public void setCodigoServicoPadrao(String codigoServicoPadrao) {
        this.codigoServicoPadrao = codigoServicoPadrao;
    }

    public BigDecimal getAliquotaIssPadrao() {
        return aliquotaIssPadrao;
    }

    public void setAliquotaIssPadrao(BigDecimal aliquotaIssPadrao) {
        this.aliquotaIssPadrao = aliquotaIssPadrao;
    }
}