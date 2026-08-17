package com.climaservice.api.repository;

import com.climaservice.api.entity.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import com.climaservice.api.entity.StatusEquipamento;

import java.util.List;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    List<Equipamento> findByClienteId(Long clienteId);
    List<Equipamento> findByClienteIdAndStatus(Long clienteId, StatusEquipamento status);
}