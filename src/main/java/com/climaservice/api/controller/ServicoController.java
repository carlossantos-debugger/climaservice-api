package com.climaservice.api.controller;

import com.climaservice.api.dto.ServicoRequestDTO;
import com.climaservice.api.dto.ServicoResponseDTO;
import com.climaservice.api.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @PostMapping
    public ResponseEntity<ServicoResponseDTO> salvar(
            @Valid @RequestBody ServicoRequestDTO dto) {

        ServicoResponseDTO servico =
                servicoService.salvar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servico);
    }

    @GetMapping
    public List<ServicoResponseDTO> listarTodos() {
        return servicoService.listarTodos();
    }

    @GetMapping("/ativos")
    public List<ServicoResponseDTO> listarAtivos() {
        return servicoService.listarAtivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return servicoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ServicoRequestDTO dto) {

        return ResponseEntity.ok(
                servicoService.atualizar(id, dto)
        );
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<ServicoResponseDTO> inativar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                servicoService.inativar(id)
        );
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<ServicoResponseDTO> ativar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                servicoService.ativar(id)
        );
    }
}