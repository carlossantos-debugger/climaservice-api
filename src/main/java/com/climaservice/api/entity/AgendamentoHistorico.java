package com.climaservice.api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento_historico")
public class AgendamentoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 20)
    private StatusAgendamento statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 20)
    private StatusAgendamento statusNovo;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public AgendamentoHistorico() {
    }

    public AgendamentoHistorico(Agendamento agendamento, StatusAgendamento statusAnterior, StatusAgendamento statusNovo, Usuario usuario) {

        this.agendamento = agendamento;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.usuario = usuario;
        this.dataAlteracao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public StatusAgendamento getStatusAnterior() {
        return statusAnterior;
    }

    public StatusAgendamento getStatusNovo() {
        return statusNovo;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
