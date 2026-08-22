package com.climaservice.api.repository;

import com.climaservice.api.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository
        extends JpaRepository<Servico, Long> {

    List<Servico> findByAtivoTrueOrderByNomeAsc();
}