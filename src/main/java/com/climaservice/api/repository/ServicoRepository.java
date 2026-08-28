package com.climaservice.api.repository;

import com.climaservice.api.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository
        extends JpaRepository<Servico, Long> {

    List<Servico> findByEmpresa_IdOrderByNomeAsc(
            Long empresaId
    );

    List<Servico> findByEmpresa_IdAndAtivoTrueOrderByNomeAsc(
            Long empresaId
    );

    Optional<Servico> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );
}