package com.climaservice.api.controller;

import com.climaservice.api.dto.*;
import com.climaservice.api.service.OrcamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.climaservice.api.dto.AtualizarOrcamentoItemRequestDTO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/ordens-servico/{ordemServicoId}/orcamentos")
    public ResponseEntity<OrcamentoResponseDTO> criar(@PathVariable Long ordemServicoId, @Valid @RequestBody OrcamentoRequestDTO dto) {

        OrcamentoResponseDTO orcamento = orcamentoService.criar(ordemServicoId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(orcamento);
    }

    @GetMapping("/ordens-servico/{ordemServicoId}/orcamentos")
    public List<OrcamentoResponseDTO> listarPorOrdemServico(@PathVariable Long ordemServicoId) {

        return orcamentoService.listarPorOrdemServico(ordemServicoId);
    }

    @GetMapping("/orcamentos/{id}")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(orcamentoService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/orcamentos/{orcamentoId}/itens/servicos")
    public ResponseEntity<OrcamentoItemResponseDTO> adicionarItem(@PathVariable Long orcamentoId, @Valid @RequestBody OrcamentoItemRequestDTO dto) {

        OrcamentoItemResponseDTO item = orcamentoService.adicionarItem(orcamentoId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @GetMapping("/orcamentos/{orcamentoId}/itens")
    public List<OrcamentoItemResponseDTO> listarItens(@PathVariable Long orcamentoId) {

        return orcamentoService.listarItens(orcamentoId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/orcamentos/{id}/status")
    public ResponseEntity<OrcamentoResponseDTO> atualizarStatus(@PathVariable Long id, @Valid @RequestBody AtualizarStatusOrcamentoRequestDTO dto) {

        return ResponseEntity.ok(orcamentoService.atualizarStatus(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PutMapping("/orcamentos/{orcamentoId}/itens/{itemId}")
    public ResponseEntity<OrcamentoItemResponseDTO> atualizarItem(@PathVariable Long orcamentoId, @PathVariable Long itemId, @Valid @RequestBody AtualizarOrcamentoItemRequestDTO dto) {

        return ResponseEntity.ok(orcamentoService.atualizarItem(orcamentoId, itemId, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @DeleteMapping("/orcamentos/{orcamentoId}/itens/{itemId}")
    public ResponseEntity<Void> removerItem(@PathVariable Long orcamentoId, @PathVariable Long itemId) {

        orcamentoService.removerItem(orcamentoId, itemId);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/orcamentos/{orcamentoId}/itens/produtos")
    public ResponseEntity<OrcamentoItemResponseDTO> adicionarProduto(@PathVariable Long orcamentoId, @Valid @RequestBody OrcamentoProdutoItemRequestDTO dto) {

        OrcamentoItemResponseDTO item = orcamentoService.adicionarProduto(orcamentoId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }
}