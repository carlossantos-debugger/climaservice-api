package com.climaservice.api.repository;

import com.climaservice.api.entity.Pagamento;
import com.climaservice.api.entity.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoRepository
        extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByOrcamentoIdOrderByDataCriacaoAsc(
            Long orcamentoId
    );

    List<Pagamento> findByOrcamentoIdAndStatus(
            Long orcamentoId,
            StatusPagamento status
    );
}