package com.climaservice.api.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "orcamento_item")
public class OrcamentoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orcamento_id", nullable = false)
    private Orcamento orcamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoItemOrcamento tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(
            name = "valor_unitario",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal valorUnitario;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal;

    public OrcamentoItem() {
    }

    public OrcamentoItem(
            Orcamento orcamento,
            TipoItemOrcamento tipo,
            Servico servico,
            String descricao,
            Integer quantidade,
            BigDecimal valorUnitario) {

        this.orcamento = orcamento;
        this.tipo = tipo;
        this.servico = servico;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;

        recalcularSubtotal();
    }

    public Long getId() {
        return id;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public TipoItemOrcamento getTipo() {
        return tipo;
    }

    public Servico getServico() {
        return servico;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void atualizar(
            Integer quantidade,
            BigDecimal valorUnitario) {

        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;

        recalcularSubtotal();
    }

    private void recalcularSubtotal() {

        this.subtotal = valorUnitario.multiply(
                BigDecimal.valueOf(quantidade)
        );
    }
}