package com.climaservice.api.service;

import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.Usuario;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoService(UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obterUsuarioAtual() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return usuarioRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
    }

    public Empresa obterEmpresaAtual() {

        Usuario usuarioAtual = obterUsuarioAtual();

        return usuarioAtual.getEmpresa();
    }
}