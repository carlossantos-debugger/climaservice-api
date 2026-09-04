package com.climaservice.api.repository;

import com.climaservice.api.entity.Pagamento;
import com.climaservice.api.entity.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}