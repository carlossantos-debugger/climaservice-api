package com.climaservice.api.repository;

import com.climaservice.api.entity.OrdemServico;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long>, JpaSpecificationExecutor<OrdemServico> {

    @EntityGraph(attributePaths = {"cliente", "equipamento"})
    List<OrdemServico> findByEmpresa_IdOrderByDataAberturaDesc(Long empresaId);

    @EntityGraph(attributePaths = {"cliente", "equipamento"})
    Optional<OrdemServico> findByIdAndEmpresa_Id(Long id, Long empresaId);

    @EntityGraph(attributePaths = {"cliente", "equipamento"})
    List<OrdemServico> findByCliente_IdAndEmpresa_IdOrderByDataAberturaDesc(Long clienteId, Long empresaId);

    @EntityGraph(attributePaths = {"cliente", "equipamento"})
    List<OrdemServico> findByEquipamento_IdAndEmpresa_IdOrderByDataAberturaDesc(Long equipamentoId, Long empresaId);
}