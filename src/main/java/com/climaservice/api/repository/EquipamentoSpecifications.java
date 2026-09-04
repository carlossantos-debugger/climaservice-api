package com.climaservice.api.repository;

import com.climaservice.api.entity.Equipamento;
import com.climaservice.api.entity.StatusEquipamento;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/*
 * Constrói os filtros de GET /equipamentos dinamicamente.
 *
 * Evita o idioma "(:param IS NULL OR ...)" em JPQL, que
 * pode falhar no driver do PostgreSQL quando o parâmetro
 * só aparece em uma comparação "IS NULL".
 */
public final class EquipamentoSpecifications {

    private EquipamentoSpecifications() {
    }

    public static Specification<Equipamento> comFiltros(Long empresaId, Long clienteId, StatusEquipamento status, String marca, String modelo) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("empresa").get("id"), empresaId));

            if (clienteId != null) {

                predicates.add(criteriaBuilder.equal(root.get("cliente").get("id"), clienteId));
            }

            if (status != null) {

                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (marca != null && !marca.isBlank()) {

                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("marca")), "%" + marca.toLowerCase() + "%"));
            }

            if (modelo != null && !modelo.isBlank()) {

                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("modelo")), "%" + modelo.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
