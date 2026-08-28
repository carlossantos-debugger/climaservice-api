package com.climaservice.api.controller;

import com.climaservice.api.dto.AgendamentoHistoricoResponseDTO;
import com.climaservice.api.dto.AgendamentoReagendarRequestDTO;
import com.climaservice.api.dto.AgendamentoRequestDTO;
import com.climaservice.api.dto.AgendamentoResponseDTO;
import com.climaservice.api.dto.AtualizarStatusAgendamentoRequestDTO;
import com.climaservice.api.entity.StatusAgendamento;
import com.climaservice.api.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping("/agendamentos")
    public ResponseEntity<AgendamentoResponseDTO> salvar(@Valid @RequestBody AgendamentoRequestDTO dto) {

        AgendamentoResponseDTO agendamento = agendamentoService.criar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(agendamento);
    }

    @GetMapping("/agendamentos")
    public List<AgendamentoResponseDTO> listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicial, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFinal, @RequestParam(required = false) Long tecnicoId, @RequestParam(required = false) StatusAgendamento status) {

        return agendamentoService.listar(dataInicial, dataFinal, tecnicoId, status);
    }

    @GetMapping("/agendamentos/{id}")
    public ResponseEntity<AgendamentoResponseDTO> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @PatchMapping("/agendamentos/{id}/status")
    public ResponseEntity<AgendamentoResponseDTO> atualizarStatus(@PathVariable Long id, @Valid @RequestBody AtualizarStatusAgendamentoRequestDTO dto) {

        return ResponseEntity.ok(agendamentoService.atualizarStatus(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/agendamentos/{id}/reagendar")
    public ResponseEntity<AgendamentoResponseDTO> reagendar(@PathVariable Long id, @Valid @RequestBody AgendamentoReagendarRequestDTO dto) {

        return ResponseEntity.ok(agendamentoService.reagendar(id, dto));
    }

    @GetMapping("/agendamentos/{id}/historico")
    public List<AgendamentoHistoricoResponseDTO> listarHistorico(@PathVariable Long id) {

        return agendamentoService.listarHistorico(id);
    }

    @GetMapping("/tecnicos/{tecnicoId}/agendamentos")
    public List<AgendamentoResponseDTO> listarPorTecnico(@PathVariable Long tecnicoId) {

        return agendamentoService.listarPorTecnico(tecnicoId);
    }

    @GetMapping("/ordens-servico/{ordemServicoId}/agendamentos")
    public List<AgendamentoResponseDTO> listarPorOrdemServico(@PathVariable Long ordemServicoId) {

        return agendamentoService.listarPorOrdemServico(ordemServicoId);
    }
}
