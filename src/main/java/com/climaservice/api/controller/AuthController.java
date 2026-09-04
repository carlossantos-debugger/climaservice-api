package com.climaservice.api.controller;

import com.climaservice.api.dto.LoginRequestDTO;
import com.climaservice.api.dto.LoginResponseDTO;
import com.climaservice.api.dto.RegisterCompanyRequestDTO;
import com.climaservice.api.dto.RegisterCompanyResponseDTO;
import com.climaservice.api.service.AuthService;
import com.climaservice.api.service.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final EmpresaService empresaService;

    public AuthController(AuthService authService, EmpresaService empresaService) {
        this.authService = authService;
        this.empresaService = empresaService;
    }

    @Operation(summary = "Autenticar por e-mail e senha e obter um JWT")
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {

        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Cadastrar empresa e seu primeiro administrador (endpoint público)")
    @SecurityRequirements
    @PostMapping("/register-company")
    public ResponseEntity<RegisterCompanyResponseDTO> registerCompany(@Valid @RequestBody RegisterCompanyRequestDTO dto) {

        RegisterCompanyResponseDTO resposta = empresaService.registrarEmpresa(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }
}