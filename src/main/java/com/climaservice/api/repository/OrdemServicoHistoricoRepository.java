package com.climaservice.api.repository;

import com.climaservice.api.entity.OrdemServicoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemServicoHistoricoRepository
        extends JpaRepository<OrdemServicoHistorico, Long> {

    List<OrdemServicoHistorico>
    findByOrdemServicoIdOrderByDataAlteracaoAsc(Long ordemServicoId);
}