package com.climaservice.api.repository;

import com.climaservice.api.entity.OrcamentoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrcamentoItemRepository
        extends JpaRepository<OrcamentoItem, Long> {

    List<OrcamentoItem> findByOrcamentoIdOrderByIdAsc(
            Long orcamentoId
    );
}