package com.climaservice.api.repository;

import com.climaservice.api.entity.FormaPagamento;
import com.climaservice.api.entity.Pagamento;
import com.climaservice.api.entity.StatusPagamento;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Constrói os filtros de GET /pagamentos dinamicamente.
 *
 * Evita o idioma "(:param IS NULL OR ...)" em JPQL, que
 * pode falhar no driver do PostgreSQL quando o parâmetro
 * só aparece em uma comparação "IS NULL".
 *
 * Pagamento não possui empresa_id próprio — o tenant é
 * derivado através de Pagamento -> Orcamento -> Empresa.
 */
public final class PagamentoSpecifications {

    private PagamentoSpecifications() {
    }

    public static Specification<Pagamento> comFiltros(Long empresaId, StatusPagamento status, FormaPagamento formaPagamento, LocalDateTime dataInicial, LocalDateTime dataFinal) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("orcamento").get("empresa").get("id"), empresaId));

            if (status != null) {

                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (formaPagamento != null) {

                predicates.add(criteriaBuilder.equal(root.get("formaPagamento"), formaPagamento));
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
