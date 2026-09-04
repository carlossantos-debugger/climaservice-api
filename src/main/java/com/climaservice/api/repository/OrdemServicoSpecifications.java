package com.climaservice.api.repository;

import com.climaservice.api.entity.OrdemServico;
import com.climaservice.api.entity.StatusOrdemServico;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Constrói os filtros de GET /ordens-servico dinamicamente.
 *
 * Evita o idioma "(:param IS NULL OR ...)" em JPQL, que
 * pode falhar no driver do PostgreSQL quando o parâmetro
 * só aparece em uma comparação "IS NULL".
 */
public final class OrdemServicoSpecifications {

    private OrdemServicoSpecifications() {
    }

    public static Specification<OrdemServico> comFiltros(Long empresaId, StatusOrdemServico status, Long clienteId, Long equipamentoId, LocalDateTime dataInicial, LocalDateTime dataFinal) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("empresa").get("id"), empresaId));

            if (status != null) {

                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (clienteId != null) {

                predicates.add(criteriaBuilder.equal(root.get("cliente").get("id"), clienteId));
            }

            if (equipamentoId != null) {

                predicates.add(criteriaBuilder.equal(root.get("equipamento").get("id"), equipamentoId));
            }

            if (dataInicial != null) {

                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dataAbertura"), dataInicial));
            }

            if (dataFinal != null) {

                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dataAbertura"), dataFinal));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
