package com.climaservice.api.repository;

import com.climaservice.api.entity.Orcamento;
import com.climaservice.api.entity.StatusOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrcamentoRepository
        extends JpaRepository<Orcamento, Long>, JpaSpecificationExecutor<Orcamento> {

    List<Orcamento> findByOrdemServico_IdAndEmpresa_IdOrderByDataCriacaoDesc(
            Long ordemServicoId,
            Long empresaId
    );

    Optional<Orcamento> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );

    /*
     * Usadas pelo dashboard.
     */
    long countByEmpresa_IdAndStatus(
            Long empresaId,
            StatusOrcamento status
    );

    @Query("SELECT COALESCE(SUM(o.valorTotal), 0) FROM Orcamento o WHERE o.empresa.id = :empresaId AND o.status = :status")
    BigDecimal somarValorTotalPorStatus(
            @Param("empresaId") Long empresaId,
            @Param("status") StatusOrcamento status
    );
}