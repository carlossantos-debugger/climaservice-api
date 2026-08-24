package com.climaservice.api.controller;

import com.climaservice.api.dto.EquipamentoRequestDTO;
import com.climaservice.api.dto.EquipamentoResponseDTO;
import com.climaservice.api.service.EquipamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EquipamentoController {

    private final EquipamentoService equipamentoService;

    public EquipamentoController(EquipamentoService equipamentoService) {
        this.equipamentoService = equipamentoService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/equipamentos")
    public ResponseEntity<EquipamentoResponseDTO> salvar(@Valid @RequestBody EquipamentoRequestDTO dto) {

        EquipamentoResponseDTO equipamento = equipamentoService.salvar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(equipamento);
    }

    @GetMapping("/equipamentos")
    public List<EquipamentoResponseDTO> listarTodos() {
        return equipamentoService.listarTodos();
    }

    @GetMapping("/equipamentos/{id}")
    public ResponseEntity<EquipamentoResponseDTO> buscarPorId(@PathVariable Long id) {

        return equipamentoService.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PutMapping("/equipamentos/{id}")
    public ResponseEntity<EquipamentoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody EquipamentoRequestDTO dto) {

        return equipamentoService.atualizar(id, dto).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/equipamentos/{id}/ativar")
    public ResponseEntity<EquipamentoResponseDTO> ativar(@PathVariable Long id) {

        return ResponseEntity.ok(equipamentoService.ativar(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/equipamentos/{id}/inativar")
    public ResponseEntity<EquipamentoResponseDTO> inativar(@PathVariable Long id) {

        return ResponseEntity.ok(equipamentoService.inativar(id));
    }

    @GetMapping("/clientes/{clienteId}/equipamentos/ativos")
    public List<EquipamentoResponseDTO> listarAtivosPorCliente(@PathVariable Long clienteId) {

        return equipamentoService.listarAtivosPorCliente(clienteId);
    }

    @GetMapping("/clientes/{clienteId}/equipamentos")
    public List<EquipamentoResponseDTO> listarPorCliente(@PathVariable Long clienteId) {

        return equipamentoService.listarPorCliente(clienteId);
    }
}