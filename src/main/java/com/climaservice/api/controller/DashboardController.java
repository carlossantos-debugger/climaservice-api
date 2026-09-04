package com.climaservice.api.controller;

import com.climaservice.api.dto.DashboardFinanceiroResponseDTO;
import com.climaservice.api.dto.DashboardOperacionalResponseDTO;
import com.climaservice.api.dto.DashboardResumoResponseDTO;
import com.climaservice.api.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard")
@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Métricas gerais do tenant autenticado (clientes, equipamentos, OS, orçamentos, agendamentos, receita do mês)")
    @GetMapping("/dashboard/resumo")
    public DashboardResumoResponseDTO resumo() {

        return dashboardService.obterResumo();
    }

    @Operation(summary = "Métricas financeiras do tenant autenticado (valor recebido, pendente, ticket médio)")
    @GetMapping("/dashboard/financeiro")
    public DashboardFinanceiroResponseDTO financeiro() {

        return dashboardService.obterFinanceiro();
    }

    @Operation(summary = "Métricas operacionais do tenant autenticado (OS por status, próximos agendamentos, manutenções preventivas próximas)")
    @GetMapping("/dashboard/operacional")
    public DashboardOperacionalResponseDTO operacional() {

        return dashboardService.obterOperacional();
    }
}
