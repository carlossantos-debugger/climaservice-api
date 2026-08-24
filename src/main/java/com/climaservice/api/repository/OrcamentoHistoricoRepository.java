package com.climaservice.api.repository;

import com.climaservice.api.entity.OrcamentoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrcamentoHistoricoRepository
        extends JpaRepository<OrcamentoHistorico, Long> {

    List<OrcamentoHistorico>
    findByOrcamentoIdOrderByDataAlteracaoAsc(Long orcamentoId);
}