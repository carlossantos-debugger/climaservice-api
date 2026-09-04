package com.climaservice.api.controller;

import com.climaservice.api.dto.DashboardFinanceiroResponseDTO;
import com.climaservice.api.dto.DashboardOperacionalResponseDTO;
import com.climaservice.api.dto.DashboardResumoResponseDTO;
import com.climaservice.api.service.DashboardService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/resumo")
    public DashboardResumoResponseDTO resumo() {

        return dashboardService.obterResumo();
    }

    @GetMapping("/dashboard/financeiro")
    public DashboardFinanceiroResponseDTO financeiro() {

        return dashboardService.obterFinanceiro();
    }

    @GetMapping("/dashboard/operacional")
    public DashboardOperacionalResponseDTO operacional() {

        return dashboardService.obterOperacional();
    }
}
