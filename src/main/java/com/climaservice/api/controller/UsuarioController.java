package com.climaservice.api.controller;

import com.climaservice.api.dto.UsuarioCadastroRequestDTO;
import com.climaservice.api.dto.UsuarioResponseDTO;
import com.climaservice.api.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioCadastroRequestDTO dto) {

        UsuarioResponseDTO usuario = usuarioService.cadastrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @GetMapping
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<UsuarioResponseDTO> inativar(@PathVariable Long id) {

        return ResponseEntity.ok(usuarioService.inativar(id));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<UsuarioResponseDTO> ativar(@PathVariable Long id) {

        return ResponseEntity.ok(usuarioService.ativar(id));
    }
}