package com.climaservice.api.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "nota_fiscal_servico")
public class NotaFiscalServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orcamento_id", nullable = false)
    private Orcamento orcamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusNotaFiscalServico status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AmbienteNotaFiscal ambiente;

    @Column(name = "discriminacao_servico", nullable = false, length = 1000)
    private String discriminacaoServico;

    @Column(name = "codigo_servico", length = 20)
    private String codigoServico;

    @Column(name = "aliquota_iss", precision = 5, scale = 2)
    private BigDecimal aliquotaIss;

    @Column(name = "valor_servico", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorServico;

    @Column(name = "valor_iss", precision = 12, scale = 2)
    private BigDecimal valorIss;

    @Column(name = "numero_nota", length = 30)
    private String numeroNota;

    @Column(name = "codigo_verificacao", length = 50)
    private String codigoVerificacao;

    @Column(name = "motivo_rejeicao", length = 1000)
    private String motivoRejeicao;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    /*
     * Representação JSON interna provisória dos dados que seriam
     * enviados à prefeitura — NÃO é o XML/DPS oficial do Sistema
     * Nacional de NFS-e. O schema exato será mapeado na Fase 2,
     * depois de conferir o Swagger/XSD ao vivo do ambiente de
     * homologação.
     */
    @Column(name = "payload_montado", columnDefinition = "TEXT")
    private String payloadMontado;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    public NotaFiscalServico() {
    }

    public NotaFiscalServico(OrdemServico ordemServico, Orcamento orcamento, String discriminacaoServico, String codigoServico, BigDecimal aliquotaIss, BigDecimal valorServico, BigDecimal valorIss, AmbienteNotaFiscal ambiente, Empresa empresa) {

        this.ordemServico = ordemServico;
        this.orcamento = orcamento;
        this.discriminacaoServico = discriminacaoServico;
        this.codigoServico = codigoServico;
        this.aliquotaIss = aliquotaIss;
        this.valorServico = valorServico;
        this.valorIss = valorIss;
        this.ambiente = ambiente;
        this.status = StatusNotaFiscalServico.RASCUNHO;
        this.dataCriacao = LocalDateTime.now();
        this.empresa = empresa;
    }

    public Long getId() {
        return id;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public StatusNotaFiscalServico getStatus() {
        return status;
    }

    public void setStatus(StatusNotaFiscalServico status) {
        this.status = status;
    }

    public AmbienteNotaFiscal getAmbiente() {
        return ambiente;
    }

    public String getDiscriminacaoServico() {
        return discriminacaoServico;
    }

    public void setDiscriminacaoServico(String discriminacaoServico) {
        this.discriminacaoServico = discriminacaoServico;
    }

    public String getCodigoServico() {
        return codigoServico;
    }

    public void setCodigoServico(String codigoServico) {
        this.codigoServico = codigoServico;
    }

    public BigDecimal getAliquotaIss() {
        return aliquotaIss;
    }

    public void setAliquotaIss(BigDecimal aliquotaIss) {
        this.aliquotaIss = aliquotaIss;
    }

    public BigDecimal getValorServico() {
        return valorServico;
    }

    public BigDecimal getValorIss() {
        return valorIss;
    }

    public void setValorIss(BigDecimal valorIss) {
        this.valorIss = valorIss;
    }

    public String getNumeroNota() {
        return numeroNota;
    }

    public String getCodigoVerificacao() {
        return codigoVerificacao;
    }

    public String getMotivoRejeicao() {
        return motivoRejeicao;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public String getPayloadMontado() {
        return payloadMontado;
    }

    public void setPayloadMontado(String payloadMontado) {
        this.payloadMontado = payloadMontado;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public Empresa getEmpresa() {
        return empresa;
    }
}
