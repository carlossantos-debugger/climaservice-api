package com.climaservice.api.repository;

import com.climaservice.api.entity.PlanoManutencaoPreventivaExecucao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlanoManutencaoPreventivaExecucaoRepository
        extends JpaRepository<PlanoManutencaoPreventivaExecucao, Long> {

    /*
     * Garante idempotência da geração de OS preventiva:
     * a mesma ocorrência (plano + data de referência) nunca
     * é executada duas vezes.
     */
    boolean existsByPlano_IdAndDataReferencia(
            Long planoId,
            LocalDate dataReferencia
    );

    @EntityGraph(attributePaths = {"ordemServico", "usuario"})
    List<PlanoManutencaoPreventivaExecucao> findByPlano_IdOrderByDataExecucaoAsc(
            Long planoId
    );
}
