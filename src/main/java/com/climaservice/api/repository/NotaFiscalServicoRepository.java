package com.climaservice.api.repository;

import com.climaservice.api.entity.NotaFiscalServico;
import com.climaservice.api.entity.StatusNotaFiscalServico;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface NotaFiscalServicoRepository
        extends JpaRepository<NotaFiscalServico, Long>, JpaSpecificationExecutor<NotaFiscalServico> {

    @EntityGraph(attributePaths = {"ordemServico", "orcamento"})
    Optional<NotaFiscalServico> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );

    /*
     * Evita emitir duas notas ativas para a mesma OS — uma nota
     * cancelada não conta, permitindo criar uma nova em seu lugar.
     */
    boolean existsByOrdemServico_IdAndEmpresa_IdAndStatusNot(
            Long ordemServicoId,
            Long empresaId,
            StatusNotaFiscalServico status
    );
}
