package com.climaservice.api.repository;

import com.climaservice.api.entity.OrdemServicoDiagnosticoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemServicoDiagnosticoHistoricoRepository
        extends JpaRepository<OrdemServicoDiagnosticoHistorico, Long> {

    List<OrdemServicoDiagnosticoHistorico>
    findByOrdemServicoIdOrderByDataAlteracaoAsc(Long ordemServicoId);
}