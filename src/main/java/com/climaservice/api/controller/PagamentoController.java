package com.climaservice.api.controller;

import com.climaservice.api.dto.PagamentoHistoricoResponseDTO;
import com.climaservice.api.dto.PagamentoRequestDTO;
import com.climaservice.api.dto.PagamentoResponseDTO;
import com.climaservice.api.dto.PagamentoResumoResponseDTO;
import com.climaservice.api.dto.PageResponseDTO;
import com.climaservice.api.entity.FormaPagamento;
import com.climaservice.api.entity.StatusPagamento;
import com.climaservice.api.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Pagamentos")
@RestController
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @Operation(summary = "Registrar pagamento de um orçamento aprovado")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/orcamentos/{orcamentoId}/pagamentos")
    public ResponseEntity<PagamentoResponseDTO> criar(@PathVariable Long orcamentoId, @Valid @RequestBody PagamentoRequestDTO dto) {

        PagamentoResponseDTO pagamento = pagamentoService.criar(orcamentoId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(pagamento);
    }

    @Operation(summary = "Listar pagamentos de um orçamento")
    @GetMapping("/orcamentos/{orcamentoId}/pagamentos")
    public List<PagamentoResponseDTO> listarPorOrcamento(@PathVariable Long orcamentoId) {

        return pagamentoService.listarPorOrcamento(orcamentoId);
    }

    @Operation(summary = "Listar pagamentos da empresa autenticada, paginado, com filtros opcionais por status, forma de pagamento e período")
    @GetMapping("/pagamentos")
    public PageResponseDTO<PagamentoResponseDTO> listar(@RequestParam(required = false) StatusPagamento status, @RequestParam(required = false) FormaPagamento formaPagamento, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicial, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFinal, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

        return pagamentoService.listar(status, formaPagamento, dataInicial, dataFinal, page, size);
    }

    @Operation(summary = "Consultar resumo financeiro de um orçamento (valor total, confirmado, pendente e disponível)")
    @GetMapping("/orcamentos/{orcamentoId}/pagamentos/resumo")
    public ResponseEntity<PagamentoResumoResponseDTO> obterResumo(@PathVariable Long orcamentoId) {

        return ResponseEntity.ok(pagamentoService.obterResumo(orcamentoId));
    }

    @Operation(summary = "Buscar pagamento por ID")
    @GetMapping("/pagamentos/{id}")
    public ResponseEntity<PagamentoResponseDTO> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(pagamentoService.buscarPorId(id));
    }

    @Operation(summary = "Confirmar pagamento pendente")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/pagamentos/{id}/confirmar")
    public ResponseEntity<PagamentoResponseDTO> confirmar(@PathVariable Long id) {

        return ResponseEntity.ok(pagamentoService.confirmar(id));
    }

    @Operation(summary = "Cancelar pagamento pendente")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/pagamentos/{id}/cancelar")
    public ResponseEntity<PagamentoResponseDTO> cancelar(@PathVariable Long id) {

        return ResponseEntity.ok(pagamentoService.cancelar(id));
    }

    @Operation(summary = "Consultar histórico de status do pagamento")
    @GetMapping("/pagamentos/{id}/historico")
    public List<PagamentoHistoricoResponseDTO> listarHistorico(@PathVariable Long id) {

        return pagamentoService.listarHistorico(id);
    }
}