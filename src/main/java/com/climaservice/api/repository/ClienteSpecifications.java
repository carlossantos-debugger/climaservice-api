package com.climaservice.api.repository;

import com.climaservice.api.entity.Cliente;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/*
 * Constrói os filtros de GET /clientes dinamicamente.
 *
 * Evita o idioma "(:param IS NULL OR ...)" em JPQL, que
 * pode falhar no driver do PostgreSQL quando o parâmetro
 * só aparece em uma comparação "IS NULL".
 */
public final class ClienteSpecifications {

    private ClienteSpecifications() {
    }

    /*
     * "nome" é busca parcial (contains, case-insensitive) — útil para
     * busca incremental no frontend. "cpfCnpj" é busca exata, já que é
     * um identificador de negócio consultado por valor completo.
     */
    public static Specification<Cliente> comFiltros(Long empresaId, String nome, String cpfCnpj) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("empresa").get("id"), empresaId));

            if (nome != null && !nome.isBlank()) {

                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
            }

            if (cpfCnpj != null && !cpfCnpj.isBlank()) {

                predicates.add(criteriaBuilder.equal(root.get("cpfCnpj"), cpfCnpj));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
