package com.climaservice.api.controller;

import com.climaservice.api.dto.ServicoRequestDTO;
import com.climaservice.api.dto.ServicoResponseDTO;
import com.climaservice.api.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Tag(name = "Catálogo de Serviços")
@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @Operation(summary = "Cadastrar serviço")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping
    public ResponseEntity<ServicoResponseDTO> salvar(@Valid @RequestBody ServicoRequestDTO dto) {

        ServicoResponseDTO servico = servicoService.salvar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(servico);
    }

    @Operation(summary = "Listar todos os serviços da empresa autenticada")
    @GetMapping
    public List<ServicoResponseDTO> listarTodos() {
        return servicoService.listarTodos();
    }

    @Operation(summary = "Listar somente serviços ativos")
    @GetMapping("/ativos")
    public List<ServicoResponseDTO> listarAtivos() {
        return servicoService.listarAtivos();
    }

    @Operation(summary = "Buscar serviço por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> buscarPorId(@PathVariable Long id) {

        return servicoService.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualizar serviço")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ServicoRequestDTO dto) {

        return ResponseEntity.ok(servicoService.atualizar(id, dto));
    }

    @Operation(summary = "Ativar serviço")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<ServicoResponseDTO> ativar(@PathVariable Long id) {

        return ResponseEntity.ok(servicoService.ativar(id));
    }

    @Operation(summary = "Inativar serviço")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<ServicoResponseDTO> inativar(@PathVariable Long id) {

        return ResponseEntity.ok(servicoService.inativar(id));
    }
}