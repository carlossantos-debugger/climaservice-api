package com.climaservice.api.controller;

import com.climaservice.api.dto.TecnicoResumoDTO;
import com.climaservice.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Usuários")
@RestController
public class TecnicoController {

    private final UsuarioService usuarioService;

    public TecnicoController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar técnicos ativos da empresa autenticada (só ID e nome — endpoint aberto a ADMIN/ATENDENTE/TECNICO, ao contrário de /usuarios que é ADMIN only)")
    @GetMapping("/tecnicos")
    public List<TecnicoResumoDTO> listar() {

        return usuarioService.listarTecnicos();
    }
}
