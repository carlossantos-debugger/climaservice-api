package com.climaservice.api.controller;

import com.climaservice.api.dto.*;
import com.climaservice.api.entity.StatusOrcamento;
import com.climaservice.api.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Orçamentos")
@RestController
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @Operation(summary = "Criar orçamento para uma ordem de serviço")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/ordens-servico/{ordemServicoId}/orcamentos")
    public ResponseEntity<OrcamentoResponseDTO> criar(@PathVariable Long ordemServicoId, @Valid @RequestBody OrcamentoRequestDTO dto) {

        OrcamentoResponseDTO orcamento = orcamentoService.criar(ordemServicoId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(orcamento);
    }

    @Operation(summary = "Listar orçamentos de uma ordem de serviço")
    @GetMapping("/ordens-servico/{ordemServicoId}/orcamentos")
    public List<OrcamentoResponseDTO> listarPorOrdemServico(@PathVariable Long ordemServicoId) {

        return orcamentoService.listarPorOrdemServico(ordemServicoId);
    }

    @Operation(summary = "Listar orçamentos da empresa autenticada, paginado, com filtros opcionais por status e período de criação")
    @GetMapping("/orcamentos")
    public PageResponseDTO<OrcamentoResponseDTO> listar(@RequestParam(required = false) StatusOrcamento status, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicial, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFinal, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

        return orcamentoService.listar(status, dataInicial, dataFinal, page, size);
    }

    @Operation(summary = "Buscar orçamento por ID")
    @GetMapping("/orcamentos/{id}")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(orcamentoService.buscarPorId(id));
    }

    @Operation(summary = "Incluir item de serviço no orçamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/orcamentos/{orcamentoId}/itens/servicos")
    public ResponseEntity<OrcamentoItemResponseDTO> adicionarItem(@PathVariable Long orcamentoId, @Valid @RequestBody OrcamentoItemRequestDTO dto) {

        OrcamentoItemResponseDTO item = orcamentoService.adicionarItem(orcamentoId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @Operation(summary = "Listar itens do orçamento")
    @GetMapping("/orcamentos/{orcamentoId}/itens")
    public List<OrcamentoItemResponseDTO> listarItens(@PathVariable Long orcamentoId) {

        return orcamentoService.listarItens(orcamentoId);
    }

    @Operation(summary = "Alterar status do orçamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/orcamentos/{id}/status")
    public ResponseEntity<OrcamentoResponseDTO> atualizarStatus(@PathVariable Long id, @Valid @RequestBody AtualizarStatusOrcamentoRequestDTO dto) {

        return ResponseEntity.ok(orcamentoService.atualizarStatus(id, dto));
    }

    @Operation(summary = "Atualizar item do orçamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PutMapping("/orcamentos/{orcamentoId}/itens/{itemId}")
    public ResponseEntity<OrcamentoItemResponseDTO> atualizarItem(@PathVariable Long orcamentoId, @PathVariable Long itemId, @Valid @RequestBody AtualizarOrcamentoItemRequestDTO dto) {

        return ResponseEntity.ok(orcamentoService.atualizarItem(orcamentoId, itemId, dto));
    }

    @Operation(summary = "Remover item do orçamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @DeleteMapping("/orcamentos/{orcamentoId}/itens/{itemId}")
    public ResponseEntity<Void> removerItem(@PathVariable Long orcamentoId, @PathVariable Long itemId) {

        orcamentoService.removerItem(orcamentoId, itemId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Incluir item de produto/peça no orçamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/orcamentos/{orcamentoId}/itens/produtos")
    public ResponseEntity<OrcamentoItemResponseDTO> adicionarProduto(@PathVariable Long orcamentoId, @Valid @RequestBody OrcamentoProdutoItemRequestDTO dto) {

        OrcamentoItemResponseDTO item = orcamentoService.adicionarProduto(orcamentoId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @Operation(summary = "Consultar histórico de status do orçamento")
    @GetMapping("/orcamentos/{id}/historico")
    public List<OrcamentoHistoricoResponseDTO> listarHistorico(@PathVariable Long id) {

        return orcamentoService.listarHistorico(id);
    }
}