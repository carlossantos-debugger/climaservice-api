package com.climaservice.api.controller;

import com.climaservice.api.dto.EquipamentoRequestDTO;
import com.climaservice.api.dto.EquipamentoResponseDTO;
import com.climaservice.api.dto.PageResponseDTO;
import com.climaservice.api.entity.StatusEquipamento;
import com.climaservice.api.service.EquipamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Equipamentos")
@RestController
public class EquipamentoController {

    private final EquipamentoService equipamentoService;

    public EquipamentoController(EquipamentoService equipamentoService) {
        this.equipamentoService = equipamentoService;
    }

    @Operation(summary = "Cadastrar equipamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/equipamentos")
    public ResponseEntity<EquipamentoResponseDTO> salvar(@Valid @RequestBody EquipamentoRequestDTO dto) {

        EquipamentoResponseDTO equipamento = equipamentoService.salvar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(equipamento);
    }

    @Operation(summary = "Listar equipamentos da empresa autenticada, paginado, com filtros opcionais por cliente, status, marca e modelo")
    @GetMapping("/equipamentos")
    public PageResponseDTO<EquipamentoResponseDTO> listar(@RequestParam(required = false) Long clienteId, @RequestParam(required = false) StatusEquipamento status, @RequestParam(required = false) String marca, @RequestParam(required = false) String modelo, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

        return equipamentoService.listar(clienteId, status, marca, modelo, page, size);
    }

    @Operation(summary = "Buscar equipamento por ID")
    @GetMapping("/equipamentos/{id}")
    public ResponseEntity<EquipamentoResponseDTO> buscarPorId(@PathVariable Long id) {

        return equipamentoService.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualizar equipamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PutMapping("/equipamentos/{id}")
    public ResponseEntity<EquipamentoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody EquipamentoRequestDTO dto) {

        return equipamentoService.atualizar(id, dto).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ativar equipamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/equipamentos/{id}/ativar")
    public ResponseEntity<EquipamentoResponseDTO> ativar(@PathVariable Long id) {

        return ResponseEntity.ok(equipamentoService.ativar(id));
    }

    @Operation(summary = "Inativar equipamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/equipamentos/{id}/inativar")
    public ResponseEntity<EquipamentoResponseDTO> inativar(@PathVariable Long id) {

        return ResponseEntity.ok(equipamentoService.inativar(id));
    }

    @Operation(summary = "Listar equipamentos ativos de um cliente")
    @GetMapping("/clientes/{clienteId}/equipamentos/ativos")
    public List<EquipamentoResponseDTO> listarAtivosPorCliente(@PathVariable Long clienteId) {

        return equipamentoService.listarAtivosPorCliente(clienteId);
    }

    @Operation(summary = "Listar equipamentos de um cliente")
    @GetMapping("/clientes/{clienteId}/equipamentos")
    public List<EquipamentoResponseDTO> listarPorCliente(@PathVariable Long clienteId) {

        return equipamentoService.listarPorCliente(clienteId);
    }
}