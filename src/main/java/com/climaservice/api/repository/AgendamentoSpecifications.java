package com.climaservice.api.repository;

import com.climaservice.api.entity.Agendamento;
import com.climaservice.api.entity.StatusAgendamento;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Constrói os filtros de GET /agendamentos dinamicamente.
 *
 * Evita o idioma "(:param IS NULL OR ...)" em JPQL, que
 * pode falhar no driver do PostgreSQL quando o parâmetro
 * só aparece em uma comparação "IS NULL".
 */
public final class AgendamentoSpecifications {

    private AgendamentoSpecifications() {
    }

    public static Specification<Agendamento> comFiltros(Long empresaId, Long tecnicoId, StatusAgendamento status, LocalDateTime dataInicial, LocalDateTime dataFinal) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("empresa").get("id"), empresaId));

            if (tecnicoId != null) {

                predicates.add(criteriaBuilder.equal(root.get("tecnico").get("id"), tecnicoId));
            }

            if (status != null) {

                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (dataInicial != null) {

                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dataHoraInicio"), dataInicial));
            }

            if (dataFinal != null) {

                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dataHoraInicio"), dataFinal));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
