package com.climaservice.api.repository;

import com.climaservice.api.entity.Orcamento;
import com.climaservice.api.entity.StatusOrcamento;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Constrói os filtros de GET /orcamentos dinamicamente.
 *
 * Evita o idioma "(:param IS NULL OR ...)" em JPQL, que
 * pode falhar no driver do PostgreSQL quando o parâmetro
 * só aparece em uma comparação "IS NULL".
 */
public final class OrcamentoSpecifications {

    private OrcamentoSpecifications() {
    }

    public static Specification<Orcamento> comFiltros(Long empresaId, StatusOrcamento status, LocalDateTime dataInicial, LocalDateTime dataFinal) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("empresa").get("id"), empresaId));

            if (status != null) {

                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (dataInicial != null) {

                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dataCriacao"), dataInicial));
            }

            if (dataFinal != null) {

                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dataCriacao"), dataFinal));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
