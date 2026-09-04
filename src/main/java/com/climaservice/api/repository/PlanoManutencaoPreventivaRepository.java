package com.climaservice.api.repository;

import com.climaservice.api.entity.PlanoManutencaoPreventiva;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PlanoManutencaoPreventivaRepository
        extends JpaRepository<PlanoManutencaoPreventiva, Long>, JpaSpecificationExecutor<PlanoManutencaoPreventiva> {

    @EntityGraph(attributePaths = {"equipamento", "tecnicoPadrao"})
    Optional<PlanoManutencaoPreventiva> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );

    @EntityGraph(attributePaths = {"equipamento", "tecnicoPadrao"})
    List<PlanoManutencaoPreventiva> findByEquipamento_IdAndEmpresa_IdOrderByProximaExecucaoAsc(
            Long equipamentoId,
            Long empresaId
    );

    @EntityGraph(attributePaths = {"equipamento", "tecnicoPadrao"})
    List<PlanoManutencaoPreventiva> findByEmpresa_IdAndAtivoTrueAndProximaExecucaoBetweenOrderByProximaExecucaoAsc(
            Long empresaId,
            LocalDate dataInicial,
            LocalDate dataFinal
    );
}
