package com.climaservice.api.repository;

import com.climaservice.api.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository
        extends JpaRepository<Empresa, Long> {

    boolean existsByCpfCnpj(
            String cpfCnpj
    );

    boolean existsByCpfCnpjAndIdNot(
            String cpfCnpj,
            Long id
    );
}
