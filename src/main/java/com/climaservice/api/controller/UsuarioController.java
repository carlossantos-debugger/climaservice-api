package com.climaservice.api.controller;

import com.climaservice.api.dto.UsuarioCadastroRequestDTO;
import com.climaservice.api.dto.UsuarioResponseDTO;
import com.climaservice.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuários")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Cadastrar usuário na empresa autenticada (ADMIN)")
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioCadastroRequestDTO dto) {

        UsuarioResponseDTO usuario = usuarioService.cadastrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @Operation(summary = "Listar usuários da empresa autenticada (ADMIN)")
    @GetMapping
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioService.listarTodos();
    }

    @Operation(summary = "Buscar usuário por ID (ADMIN)")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @Operation(summary = "Inativar usuário (ADMIN)")
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<UsuarioResponseDTO> inativar(@PathVariable Long id) {

        return ResponseEntity.ok(usuarioService.inativar(id));
    }

    @Operation(summary = "Ativar usuário (ADMIN)")
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<UsuarioResponseDTO> ativar(@PathVariable Long id) {

        return ResponseEntity.ok(usuarioService.ativar(id));
    }
}