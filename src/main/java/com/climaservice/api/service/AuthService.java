package com.climaservice.api.service;

import com.climaservice.api.dto.LoginRequestDTO;
import com.climaservice.api.dto.LoginResponseDTO;
import com.climaservice.api.entity.Usuario;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {

        String emailNormalizado = normalizarEmail(dto.email());

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado).orElseThrow(() -> new BusinessRuleException("E-mail ou senha inválidos"));

        validarUsuarioAtivo(usuario);

        validarEmpresaAtiva(usuario);

        validarSenha(dto.senha(), usuario.getSenhaHash());

        String token = jwtService.gerarToken(usuario);

        return new LoginResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole(),token);
    }

    private String normalizarEmail(String email) {

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validarUsuarioAtivo(Usuario usuario) {

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new BusinessRuleException("Usuário inativo");
        }
    }

    private void validarEmpresaAtiva(Usuario usuario) {

        if (!usuario.getEmpresa().isAtivo()) {
            throw new BusinessRuleException("Empresa inativa");
        }
    }

    private void validarSenha(String senhaDigitada, String senhaHash) {

        boolean senhaValida = passwordEncoder.matches(senhaDigitada, senhaHash);

        if (!senhaValida) {
            throw new BusinessRuleException("E-mail ou senha inválidos");
        }
    }
}