package com.climaservice.api.controller;

import com.climaservice.api.dto.*;
import com.climaservice.api.service.OrdemServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @PostMapping("/ordens-servico")
    public ResponseEntity<OrdemServicoResponseDTO> salvar(@Valid @RequestBody OrdemServicoRequestDTO dto) {

        OrdemServicoResponseDTO ordemServico = ordemServicoService.salvar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ordemServico);
    }

    @GetMapping("/ordens-servico")
    public List<OrdemServicoResponseDTO> listarTodas() {
        return ordemServicoService.listarTodas();
    }

    @GetMapping("/ordens-servico/{id}")
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorId(@PathVariable Long id) {

        return ordemServicoService.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/clientes/{clienteId}/ordens-servico")
    public List<OrdemServicoResponseDTO> listarPorCliente(@PathVariable Long clienteId) {

        return ordemServicoService.listarPorCliente(clienteId);
    }

    @GetMapping("/equipamentos/{equipamentoId}/ordens-servico")
    public List<OrdemServicoResponseDTO> listarPorEquipamento(@PathVariable Long equipamentoId) {

        return ordemServicoService.listarPorEquipamento(equipamentoId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @PatchMapping("/ordens-servico/{id}/diagnostico")
    public ResponseEntity<OrdemServicoResponseDTO> atualizarDiagnostico(@PathVariable Long id, @Valid @RequestBody AtualizarDiagnosticoRequestDTO dto) {

        return ResponseEntity.ok(ordemServicoService.atualizarDiagnostico(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @PatchMapping("/ordens-servico/{id}/status")
    public ResponseEntity<OrdemServicoResponseDTO> atualizarStatus(@PathVariable Long id, @Valid @RequestBody AtualizarStatusOrdemServicoRequestDTO dto) {

        return ResponseEntity.ok(ordemServicoService.atualizarStatus(id, dto));
    }

    @GetMapping("/ordens-servico/{id}/historico")
    public List<OrdemServicoHistoricoResponseDTO> listarHistorico(@PathVariable Long id) {

        return ordemServicoService.listarHistorico(id);
    }


}