package com.climaservice.api.repository;

import com.climaservice.api.entity.Pagamento;
import com.climaservice.api.entity.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PagamentoRepository
        extends JpaRepository<Pagamento, Long>, JpaSpecificationExecutor<Pagamento> {

    Optional<Pagamento> findByIdAndOrcamento_Empresa_Id(
            Long pagamentoId,
            Long empresaId
    );

    List<Pagamento> findByOrcamento_IdAndOrcamento_Empresa_IdOrderByDataCriacaoAsc(
            Long orcamentoId,
            Long empresaId
    );

    List<Pagamento> findByOrcamento_IdAndOrcamento_Empresa_IdAndStatus(
            Long orcamentoId,
            Long empresaId,
            StatusPagamento status
    );

    /*
     * Usadas pelo dashboard. Pagamento não possui empresa_id
     * próprio — o tenant é derivado via Pagamento -> Orcamento -> Empresa.
     */
    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.orcamento.empresa.id = :empresaId AND p.status = :status")
    BigDecimal somarValorPorStatus(
            @Param("empresaId") Long empresaId,
            @Param("status") StatusPagamento status
    );

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.orcamento.empresa.id = :empresaId AND p.status = :status AND p.dataConfirmacao BETWEEN :inicio AND :fim")
    BigDecimal somarValorPorStatusEPeriodoConfirmacao(
            @Param("empresaId") Long empresaId,
            @Param("status") StatusPagamento status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}