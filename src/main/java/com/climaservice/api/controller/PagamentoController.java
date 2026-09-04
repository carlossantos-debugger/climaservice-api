package com.climaservice.api.controller;

import com.climaservice.api.dto.PagamentoHistoricoResponseDTO;
import com.climaservice.api.dto.PagamentoRequestDTO;
import com.climaservice.api.dto.PagamentoResponseDTO;
import com.climaservice.api.dto.PagamentoResumoResponseDTO;
import com.climaservice.api.dto.PageResponseDTO;
import com.climaservice.api.entity.FormaPagamento;
import com.climaservice.api.entity.StatusPagamento;
import com.climaservice.api.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/orcamentos/{orcamentoId}/pagamentos")
    public ResponseEntity<PagamentoResponseDTO> criar(@PathVariable Long orcamentoId, @Valid @RequestBody PagamentoRequestDTO dto) {

        PagamentoResponseDTO pagamento = pagamentoService.criar(orcamentoId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(pagamento);
    }

    @GetMapping("/orcamentos/{orcamentoId}/pagamentos")
    public List<PagamentoResponseDTO> listarPorOrcamento(@PathVariable Long orcamentoId) {

        return pagamentoService.listarPorOrcamento(orcamentoId);
    }

    @GetMapping("/pagamentos")
    public PageResponseDTO<PagamentoResponseDTO> listar(@RequestParam(required = false) StatusPagamento status, @RequestParam(required = false) FormaPagamento formaPagamento, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicial, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFinal, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

        return pagamentoService.listar(status, formaPagamento, dataInicial, dataFinal, page, size);
    }

    @GetMapping("/orcamentos/{orcamentoId}/pagamentos/resumo")
    public ResponseEntity<PagamentoResumoResponseDTO> obterResumo(@PathVariable Long orcamentoId) {

        return ResponseEntity.ok(pagamentoService.obterResumo(orcamentoId));
    }

    @GetMapping("/pagamentos/{id}")
    public ResponseEntity<PagamentoResponseDTO> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(pagamentoService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/pagamentos/{id}/confirmar")
    public ResponseEntity<PagamentoResponseDTO> confirmar(@PathVariable Long id) {

        return ResponseEntity.ok(pagamentoService.confirmar(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/pagamentos/{id}/cancelar")
    public ResponseEntity<PagamentoResponseDTO> cancelar(@PathVariable Long id) {

        return ResponseEntity.ok(pagamentoService.cancelar(id));
    }

    @GetMapping("/pagamentos/{id}/historico")
    public List<PagamentoHistoricoResponseDTO> listarHistorico(@PathVariable Long id) {

        return pagamentoService.listarHistorico(id);
    }
}