package com.climaservice.api.repository;

import com.climaservice.api.entity.NotaFiscalServico;
import com.climaservice.api.entity.StatusNotaFiscalServico;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/*
 * Constrói os filtros de GET /notas-fiscais-servico dinamicamente.
 *
 * Evita o idioma "(:param IS NULL OR ...)" em JPQL, que
 * pode falhar no driver do PostgreSQL quando o parâmetro
 * só aparece em uma comparação "IS NULL".
 */
public final class NotaFiscalServicoSpecifications {

    private NotaFiscalServicoSpecifications() {
    }

    public static Specification<NotaFiscalServico> comFiltros(Long empresaId, StatusNotaFiscalServico status) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("empresa").get("id"), empresaId));

            if (status != null) {

                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
