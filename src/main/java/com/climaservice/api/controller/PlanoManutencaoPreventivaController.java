package com.climaservice.api.controller;

import com.climaservice.api.dto.PlanoManutencaoPreventivaAtualizarRequestDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaExecucaoResponseDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaRequestDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaResponseDTO;
import com.climaservice.api.service.PlanoManutencaoPreventivaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlanoManutencaoPreventivaController {

    private final PlanoManutencaoPreventivaService planoService;

    public PlanoManutencaoPreventivaController(PlanoManutencaoPreventivaService planoService) {
        this.planoService = planoService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/planos-manutencao-preventiva")
    public ResponseEntity<PlanoManutencaoPreventivaResponseDTO> salvar(@Valid @RequestBody PlanoManutencaoPreventivaRequestDTO dto) {

        PlanoManutencaoPreventivaResponseDTO plano = planoService.criar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(plano);
    }

    @GetMapping("/planos-manutencao-preventiva")
    public List<PlanoManutencaoPreventivaResponseDTO> listar(@RequestParam(required = false) Long equipamentoId, @RequestParam(required = false) Boolean ativo) {

        return planoService.listar(equipamentoId, ativo);
    }

    @GetMapping("/planos-manutencao-preventiva/proximas")
    public List<PlanoManutencaoPreventivaResponseDTO> listarProximas(@RequestParam(required = false, defaultValue = "30") int diasLimite) {

        return planoService.listarProximas(diasLimite);
    }

    @GetMapping("/planos-manutencao-preventiva/{id}")
    public ResponseEntity<PlanoManutencaoPreventivaResponseDTO> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(planoService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PutMapping("/planos-manutencao-preventiva/{id}")
    public ResponseEntity<PlanoManutencaoPreventivaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PlanoManutencaoPreventivaAtualizarRequestDTO dto) {

        return ResponseEntity.ok(planoService.atualizar(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/planos-manutencao-preventiva/{id}/ativar")
    public ResponseEntity<PlanoManutencaoPreventivaResponseDTO> ativar(@PathVariable Long id) {

        return ResponseEntity.ok(planoService.ativar(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/planos-manutencao-preventiva/{id}/inativar")
    public ResponseEntity<PlanoManutencaoPreventivaResponseDTO> inativar(@PathVariable Long id) {

        return ResponseEntity.ok(planoService.inativar(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/planos-manutencao-preventiva/{id}/gerar-ordem-servico")
    public ResponseEntity<PlanoManutencaoPreventivaExecucaoResponseDTO> gerarOrdemServico(@PathVariable Long id) {

        return ResponseEntity.status(HttpStatus.CREATED).body(planoService.gerarOrdemServico(id));
    }

    @GetMapping("/planos-manutencao-preventiva/{id}/execucoes")
    public List<PlanoManutencaoPreventivaExecucaoResponseDTO> listarExecucoes(@PathVariable Long id) {

        return planoService.listarExecucoes(id);
    }

    @GetMapping("/equipamentos/{equipamentoId}/planos-manutencao-preventiva")
    public List<PlanoManutencaoPreventivaResponseDTO> listarPorEquipamento(@PathVariable Long equipamentoId) {

        return planoService.listarPorEquipamento(equipamentoId);
    }
}
