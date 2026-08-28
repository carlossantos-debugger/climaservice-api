package com.climaservice.api.repository;

import com.climaservice.api.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository
        extends JpaRepository<Produto, Long> {

    List<Produto> findByEmpresa_IdOrderByNomeAsc(
            Long empresaId
    );

    List<Produto> findByEmpresa_IdAndAtivoTrueOrderByNomeAsc(
            Long empresaId
    );

    Optional<Produto> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );
}