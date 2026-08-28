package com.climaservice.api.repository;

import com.climaservice.api.entity.OrcamentoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrcamentoItemRepository
        extends JpaRepository<OrcamentoItem, Long> {

    List<OrcamentoItem> findByOrcamentoIdOrderByIdAsc(
            Long orcamentoId
    );

    Optional<OrcamentoItem>
    findByIdAndOrcamento_IdAndOrcamento_Empresa_Id(
            Long itemId,
            Long orcamentoId,
            Long empresaId
    );
}