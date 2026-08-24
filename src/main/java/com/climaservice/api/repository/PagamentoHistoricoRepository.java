package com.climaservice.api.repository;

import com.climaservice.api.entity.PagamentoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoHistoricoRepository
        extends JpaRepository<PagamentoHistorico, Long> {

    List<PagamentoHistorico>
    findByPagamentoIdOrderByDataAlteracaoAsc(Long pagamentoId);
}