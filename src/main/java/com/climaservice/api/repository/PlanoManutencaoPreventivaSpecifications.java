package com.climaservice.api.repository;

import com.climaservice.api.entity.PlanoManutencaoPreventiva;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/*
 * Constrói os filtros de GET /planos-manutencao-preventiva dinamicamente.
 *
 * Evita o idioma "(:param IS NULL OR ...)" em JPQL, que
 * pode falhar no driver do PostgreSQL quando o parâmetro
 * só aparece em uma comparação "IS NULL".
 */
public final class PlanoManutencaoPreventivaSpecifications {

    private PlanoManutencaoPreventivaSpecifications() {
    }

    public static Specification<PlanoManutencaoPreventiva> comFiltros(Long empresaId, Long equipamentoId, Boolean ativo) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("empresa").get("id"), empresaId));

            if (equipamentoId != null) {

                predicates.add(criteriaBuilder.equal(root.get("equipamento").get("id"), equipamentoId));
            }

            if (ativo != null) {

                predicates.add(criteriaBuilder.equal(root.get("ativo"), ativo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
