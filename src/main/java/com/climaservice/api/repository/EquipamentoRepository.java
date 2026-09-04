package com.climaservice.api.repository;

import com.climaservice.api.entity.Equipamento;
import com.climaservice.api.entity.StatusEquipamento;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface EquipamentoRepository
        extends JpaRepository<Equipamento, Long>, JpaSpecificationExecutor<Equipamento> {

    @EntityGraph(attributePaths = "cliente")
    List<Equipamento> findByEmpresa_IdOrderByIdAsc(
            Long empresaId
    );

    @EntityGraph(attributePaths = "cliente")
    Optional<Equipamento> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );

    @EntityGraph(attributePaths = "cliente")
    List<Equipamento> findByCliente_IdAndEmpresa_Id(
            Long clienteId,
            Long empresaId
    );

    @EntityGraph(attributePaths = "cliente")
    List<Equipamento> findByCliente_IdAndStatusAndEmpresa_Id(
            Long clienteId,
            StatusEquipamento status,
            Long empresaId
    );

    long countByEmpresa_IdAndStatus(
            Long empresaId,
            StatusEquipamento status
    );
}