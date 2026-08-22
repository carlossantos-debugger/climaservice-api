package com.climaservice.api.repository;

import com.climaservice.api.entity.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemServicoRepository
        extends JpaRepository<OrdemServico, Long> {

    List<OrdemServico> findByClienteId(Long clienteId);

    List<OrdemServico> findByEquipamentoId(Long equipamentoId);
}