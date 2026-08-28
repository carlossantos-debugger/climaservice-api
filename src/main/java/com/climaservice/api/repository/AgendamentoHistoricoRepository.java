package com.climaservice.api.repository;

import com.climaservice.api.entity.AgendamentoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgendamentoHistoricoRepository
        extends JpaRepository<AgendamentoHistorico, Long> {

    List<AgendamentoHistorico>
    findByAgendamentoIdOrderByDataAlteracaoAsc(Long agendamentoId);
}
