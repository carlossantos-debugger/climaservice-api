package com.climaservice.api.controller;

import com.climaservice.api.dto.*;
import com.climaservice.api.entity.StatusOrdemServico;
import com.climaservice.api.service.OrdemServicoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
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
    public PageResponseDTO<OrdemServicoResponseDTO> listar(@RequestParam(required = false) StatusOrdemServico status, @RequestParam(required = false) Long clienteId, @RequestParam(required = false) Long equipamentoId, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicial, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFinal, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

        return ordemServicoService.listar(status, clienteId, equipamentoId, dataInicial, dataFinal, page, size);
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

    @GetMapping("/ordens-servico/{id}/historico-diagnostico")
    public List<OrdemServicoDiagnosticoHistoricoResponseDTO> listarHistoricoDiagnostico(@PathVariable Long id) {

        return ordemServicoService.listarHistoricoDiagnostico(id);
    }


}