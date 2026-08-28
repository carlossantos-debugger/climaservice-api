package com.climaservice.api.repository;

import com.climaservice.api.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrcamentoRepository
        extends JpaRepository<Orcamento, Long> {

    List<Orcamento> findByOrdemServico_IdAndEmpresa_IdOrderByDataCriacaoDesc(
            Long ordemServicoId,
            Long empresaId
    );

    Optional<Orcamento> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );
}