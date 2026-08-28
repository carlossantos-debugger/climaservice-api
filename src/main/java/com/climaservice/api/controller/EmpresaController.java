package com.climaservice.api.controller;

import com.climaservice.api.dto.EmpresaAtualizarRequestDTO;
import com.climaservice.api.dto.EmpresaResponseDTO;
import com.climaservice.api.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresa")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping("/me")
    public ResponseEntity<EmpresaResponseDTO> obterEmpresaAtual() {

        return ResponseEntity.ok(empresaService.obterEmpresaAtual());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/me")
    public ResponseEntity<EmpresaResponseDTO> atualizarEmpresaAtual(@Valid @RequestBody EmpresaAtualizarRequestDTO dto) {

        return ResponseEntity.ok(empresaService.atualizarEmpresaAtual(dto));
    }
}
