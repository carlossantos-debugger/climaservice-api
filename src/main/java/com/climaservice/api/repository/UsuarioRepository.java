package com.climaservice.api.repository;

import com.climaservice.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    /*
     * Mantido global porque o login identifica o usuário pelo e-mail.
     */
    Optional<Usuario> findByEmailIgnoreCase(
            String email
    );

    /*
     * Mantido global enquanto o e-mail for unico em toda a aplicação.
     */
    boolean existsByEmailIgnoreCase(
            String email
    );

    /*
     * Consultas multi-tenant.
     */

    List<Usuario> findByEmpresa_IdOrderByNomeAsc(
            Long empresaId
    );

    Optional<Usuario> findByIdAndEmpresa_Id(
            Long id,
            Long empresaId
    );
}