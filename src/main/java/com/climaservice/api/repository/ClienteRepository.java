package com.climaservice.api.repository;

import com.climaservice.api.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    List<Cliente> findByEmpresa_IdOrderByNomeAsc(
            Long empresaId
    );

    Optional<Cliente> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );
}