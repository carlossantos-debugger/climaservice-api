package com.climaservice.api.controller;

import com.climaservice.api.dto.PagamentoRequestDTO;
import com.climaservice.api.dto.PagamentoResponseDTO;
import com.climaservice.api.dto.PagamentoResumoResponseDTO;
import com.climaservice.api.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("/orcamentos/{orcamentoId}/pagamentos")
    public ResponseEntity<PagamentoResponseDTO> criar(
            @PathVariable Long orcamentoId,
            @Valid @RequestBody PagamentoRequestDTO dto) {

        PagamentoResponseDTO pagamento =
                pagamentoService.criar(
                        orcamentoId,
                        dto
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pagamento);
    }

    @GetMapping("/orcamentos/{orcamentoId}/pagamentos")
    public List<PagamentoResponseDTO> listarPorOrcamento(
            @PathVariable Long orcamentoId) {

        return pagamentoService
                .listarPorOrcamento(orcamentoId);
    }

    @GetMapping("/orcamentos/{orcamentoId}/pagamentos/resumo")
    public ResponseEntity<PagamentoResumoResponseDTO> obterResumo(
            @PathVariable Long orcamentoId) {

        return ResponseEntity.ok(
                pagamentoService.obterResumo(orcamentoId)
        );
    }

    @GetMapping("/pagamentos/{id}")
    public ResponseEntity<PagamentoResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                pagamentoService.buscarPorId(id)
        );
    }

    @PatchMapping("/pagamentos/{id}/confirmar")
    public ResponseEntity<PagamentoResponseDTO> confirmar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                pagamentoService.confirmar(id)
        );
    }

    @PatchMapping("/pagamentos/{id}/cancelar")
    public ResponseEntity<PagamentoResponseDTO> cancelar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                pagamentoService.cancelar(id)
        );
    }
}