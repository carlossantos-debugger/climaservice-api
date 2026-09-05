package com.climaservice.api.controller;

import com.climaservice.api.dto.NotaFiscalServicoRequestDTO;
import com.climaservice.api.dto.NotaFiscalServicoResponseDTO;
import com.climaservice.api.dto.PageResponseDTO;
import com.climaservice.api.entity.StatusNotaFiscalServico;
import com.climaservice.api.service.NotaFiscalServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Nota Fiscal de Serviço")
@RestController
public class NotaFiscalServicoController {

    private final NotaFiscalServicoService notaFiscalServicoService;

    public NotaFiscalServicoController(NotaFiscalServicoService notaFiscalServicoService) {
        this.notaFiscalServicoService = notaFiscalServicoService;
    }

    @Operation(summary = "Criar rascunho de nota fiscal de serviço a partir do orçamento aprovado da ordem de serviço")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/ordens-servico/{ordemServicoId}/nota-fiscal-servico")
    public ResponseEntity<NotaFiscalServicoResponseDTO> criar(@PathVariable Long ordemServicoId, @Valid @RequestBody NotaFiscalServicoRequestDTO dto) {

        NotaFiscalServicoResponseDTO nota = notaFiscalServicoService.criar(ordemServicoId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(nota);
    }

    @Operation(summary = "Listar notas fiscais de serviço da empresa autenticada, paginado, com filtro opcional por status")
    @GetMapping("/notas-fiscais-servico")
    public PageResponseDTO<NotaFiscalServicoResponseDTO> listar(@RequestParam(required = false) StatusNotaFiscalServico status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

        return notaFiscalServicoService.listar(status, page, size);
    }

    @Operation(summary = "Buscar nota fiscal de serviço por ID")
    @GetMapping("/notas-fiscais-servico/{id}")
    public ResponseEntity<NotaFiscalServicoResponseDTO> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(notaFiscalServicoService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar rascunho de nota fiscal de serviço")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PutMapping("/notas-fiscais-servico/{id}")
    public ResponseEntity<NotaFiscalServicoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody NotaFiscalServicoRequestDTO dto) {

        return ResponseEntity.ok(notaFiscalServicoService.atualizar(id, dto));
    }

    @Operation(summary = "Montar e validar o payload interno da nota (não envia à prefeitura)")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/notas-fiscais-servico/{id}/gerar-payload")
    public ResponseEntity<NotaFiscalServicoResponseDTO> gerarPayload(@PathVariable Long id) {

        return ResponseEntity.ok(notaFiscalServicoService.gerarPayload(id));
    }

    @Operation(summary = "Enviar a nota à prefeitura (Fase 2 — ainda não disponível, requer certificado digital)")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/notas-fiscais-servico/{id}/enviar")
    public ResponseEntity<NotaFiscalServicoResponseDTO> enviar(@PathVariable Long id) {

        return ResponseEntity.ok(notaFiscalServicoService.enviar(id));
    }

    @Operation(summary = "Cancelar rascunho de nota fiscal de serviço")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/notas-fiscais-servico/{id}/cancelar")
    public ResponseEntity<NotaFiscalServicoResponseDTO> cancelar(@PathVariable Long id) {

        return ResponseEntity.ok(notaFiscalServicoService.cancelar(id));
    }
}
