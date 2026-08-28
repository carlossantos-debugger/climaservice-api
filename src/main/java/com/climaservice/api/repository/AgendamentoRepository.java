package com.climaservice.api.repository;

import com.climaservice.api.entity.Agendamento;
import com.climaservice.api.entity.StatusAgendamento;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository
        extends JpaRepository<Agendamento, Long>, JpaSpecificationExecutor<Agendamento> {

    @EntityGraph(attributePaths = {"tecnico"})
    Optional<Agendamento> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );

    @EntityGraph(attributePaths = {"tecnico"})
    List<Agendamento> findByOrdemServico_IdAndEmpresa_IdOrderByDataHoraInicioAsc(
            Long ordemServicoId,
            Long empresaId
    );

    @EntityGraph(attributePaths = {"tecnico"})
    List<Agendamento> findByTecnico_IdAndEmpresa_IdOrderByDataHoraInicioAsc(
            Long tecnicoId,
            Long empresaId
    );

    /*
     * Utilizada para impedir sobreposição de agendamentos
     * ativos para o mesmo técnico.
     */
    @Query("""
            SELECT a FROM Agendamento a
            WHERE a.tecnico.id = :tecnicoId
              AND a.empresa.id = :empresaId
              AND a.status IN :statusAtivos
              AND a.dataHoraInicio < :dataHoraFim
              AND a.dataHoraFim > :dataHoraInicio
              AND (:agendamentoIdExcluido IS NULL OR a.id <> :agendamentoIdExcluido)
            """)
    List<Agendamento> buscarConflitantes(
            @Param("tecnicoId") Long tecnicoId,
            @Param("empresaId") Long empresaId,
            @Param("statusAtivos") Collection<StatusAgendamento> statusAtivos,
            @Param("dataHoraInicio") LocalDateTime dataHoraInicio,
            @Param("dataHoraFim") LocalDateTime dataHoraFim,
            @Param("agendamentoIdExcluido") Long agendamentoIdExcluido
    );
}
