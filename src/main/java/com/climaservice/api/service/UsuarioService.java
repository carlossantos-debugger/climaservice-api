package com.climaservice.api.service;

import com.climaservice.api.dto.UsuarioCadastroRequestDTO;
import com.climaservice.api.dto.UsuarioResponseDTO;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.Usuario;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    UsuarioAutenticadoService usuarioAutenticadoService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, UsuarioAutenticadoService usuarioAutenticadoService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public UsuarioResponseDTO cadastrar(UsuarioCadastroRequestDTO dto) {

        String emailNormalizado = normalizarEmail(dto.email());

        validarEmailDisponivel(emailNormalizado);

        String senhaHash = passwordEncoder.encode(dto.senha());

        Empresa empresa = usuarioAutenticadoService.obterEmpresaAtual();

        Usuario usuario = new Usuario(dto.nome().trim(), emailNormalizado, senhaHash, dto.role(), empresa);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return converterParaResponse(usuarioSalvo);
    }

    private String normalizarEmail(String email) {

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validarEmailDisponivel(String email) {

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {

            throw new BusinessRuleException("Já existe um usuário cadastrado com este e-mail");
        }
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {

        return usuarioRepository.findAll().stream().map(this::converterParaResponse).toList();
    }

    private Usuario buscarEntidadePorId(Long id) {

        return usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {

        Usuario usuario = buscarEntidadePorId(id);

        return converterParaResponse(usuario);
    }

    @Transactional
    public UsuarioResponseDTO inativar(Long id) {

        Usuario usuario = buscarEntidadePorId(id);

        usuario.setAtivo(false);

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);

        return converterParaResponse(usuarioAtualizado);
    }

    @Transactional
    public UsuarioResponseDTO ativar(Long id) {

        Usuario usuario = buscarEntidadePorId(id);

        usuario.setAtivo(true);

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);

        return converterParaResponse(usuarioAtualizado);
    }

    private UsuarioResponseDTO converterParaResponse(Usuario usuario) {

        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole(), usuario.getAtivo(), usuario.getDataCriacao());
    }
}