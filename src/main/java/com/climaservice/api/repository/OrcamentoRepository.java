package com.climaservice.api.repository;

import com.climaservice.api.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrcamentoRepository
        extends JpaRepository<Orcamento, Long> {

    List<Orcamento> findByOrdemServicoIdOrderByDataCriacaoDesc(
            Long ordemServicoId
    );
}