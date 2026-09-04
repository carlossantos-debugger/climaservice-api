package com.climaservice.api.repository;

import com.climaservice.api.entity.RoleUsuario;
import com.climaservice.api.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    /*
     * Mantido global porque o login identifica o usuário pelo e-mail.
     *
     * Busca a empresa junto: tanto o login (AuthService) quanto o
     * JwtAuthFilter precisam verificar usuario.getEmpresa().getAtivo()
     * a cada requisição, e não devem depender implicitamente de
     * open-in-view para isso.
     */
    @EntityGraph(attributePaths = "empresa")
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

    long countByEmpresa_IdAndRoleAndAtivoTrue(
            Long empresaId,
            RoleUsuario role
    );
}