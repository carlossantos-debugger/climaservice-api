package com.climaservice.api.service;

import com.climaservice.api.dto.AgendamentoResponseDTO;
import com.climaservice.api.dto.DashboardFinanceiroResponseDTO;
import com.climaservice.api.dto.DashboardOperacionalResponseDTO;
import com.climaservice.api.dto.DashboardResumoResponseDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaResponseDTO;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.StatusEquipamento;
import com.climaservice.api.entity.StatusOrcamento;
import com.climaservice.api.entity.StatusOrdemServico;
import com.climaservice.api.entity.StatusPagamento;
import com.climaservice.api.repository.AgendamentoRepository;
import com.climaservice.api.repository.ClienteRepository;
import com.climaservice.api.repository.EquipamentoRepository;
import com.climaservice.api.repository.OrcamentoRepository;
import com.climaservice.api.repository.OrdemServicoRepository;
import com.climaservice.api.repository.PagamentoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final Long EMPRESA_ID = 8001L;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendamentoService agendamentoService;

    @Mock
    private PlanoManutencaoPreventivaService planoManutencaoPreventivaService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private Empresa empresa;

    @InjectMocks
    private DashboardService dashboardService;

    private void prepararEmpresaAtual() {

        when(usuarioAutenticadoService.obterEmpresaAtual()).thenReturn(empresa);

        when(empresa.getId()).thenReturn(EMPRESA_ID);
    }

    @Test
    void deveRetornarResumoZeradoParaTenantVazio() {

        prepararEmpresaAtual();

        when(clienteRepository.countByEmpresa_Id(EMPRESA_ID)).thenReturn(0L);

        when(equipamentoRepository.countByEmpresa_IdAndStatus(EMPRESA_ID, StatusEquipamento.ATIVO)).thenReturn(0L);

        when(ordemServicoRepository.countByEmpresa_IdAndStatus(eq(EMPRESA_ID), any())).thenReturn(0L);

        when(orcamentoRepository.countByEmpresa_IdAndStatus(EMPRESA_ID, StatusOrcamento.ENVIADO)).thenReturn(0L);

        when(agendamentoRepository.countByEmpresa_IdAndDataHoraInicioBetweenAndStatusNot(eq(EMPRESA_ID), any(), any(), any())).thenReturn(0L);

        when(pagamentoRepository.somarValorPorStatusEPeriodoConfirmacao(eq(EMPRESA_ID), eq(StatusPagamento.CONFIRMADO), any(), any())).thenReturn(BigDecimal.ZERO);

        DashboardResumoResponseDTO resumo = dashboardService.obterResumo();

        assertEquals(0L, resumo.clientesAtivos());

        assertEquals(0L, resumo.equipamentosAtivos());

        assertEquals(0L, resumo.ordensAbertas());

        assertEquals(0L, resumo.ordensEmAndamento());

        assertEquals(0L, resumo.orcamentosPendentes());

        assertEquals(0L, resumo.agendamentosHoje());

        assertEquals(BigDecimal.ZERO, resumo.receitaConfirmadaNoMes());
    }

    @Test
    void deveCalcularResumoComDadosDoTenant() {

        prepararEmpresaAtual();

        when(clienteRepository.countByEmpresa_Id(EMPRESA_ID)).thenReturn(5L);

        when(equipamentoRepository.countByEmpresa_IdAndStatus(EMPRESA_ID, StatusEquipamento.ATIVO)).thenReturn(4L);

        when(ordemServicoRepository.countByEmpresa_IdAndStatus(EMPRESA_ID, StatusOrdemServico.ABERTA)).thenReturn(2L);

        when(ordemServicoRepository.countByEmpresa_IdAndStatus(EMPRESA_ID, StatusOrdemServico.EM_ANDAMENTO)).thenReturn(1L);

        when(orcamentoRepository.countByEmpresa_IdAndStatus(EMPRESA_ID, StatusOrcamento.ENVIADO)).thenReturn(3L);

        when(agendamentoRepository.countByEmpresa_IdAndDataHoraInicioBetweenAndStatusNot(eq(EMPRESA_ID), any(), any(), any())).thenReturn(6L);

        when(pagamentoRepository.somarValorPorStatusEPeriodoConfirmacao(eq(EMPRESA_ID), eq(StatusPagamento.CONFIRMADO), any(), any())).thenReturn(new BigDecimal("1500.00"));

        DashboardResumoResponseDTO resumo = dashboardService.obterResumo();

        assertEquals(5L, resumo.clientesAtivos());

        assertEquals(4L, resumo.equipamentosAtivos());

        assertEquals(2L, resumo.ordensAbertas());

        assertEquals(1L, resumo.ordensEmAndamento());

        assertEquals(3L, resumo.orcamentosPendentes());

        assertEquals(6L, resumo.agendamentosHoje());

        assertEquals(new BigDecimal("1500.00"), resumo.receitaConfirmadaNoMes());
    }

    @Test
    void deveCalcularTicketMedioZeradoQuandoNaoHouverOrcamentosAprovados() {

        prepararEmpresaAtual();

        when(pagamentoRepository.somarValorPorStatus(EMPRESA_ID, StatusPagamento.CONFIRMADO)).thenReturn(BigDecimal.ZERO);

        when(pagamentoRepository.somarValorPorStatus(EMPRESA_ID, StatusPagamento.PENDENTE)).thenReturn(BigDecimal.ZERO);

        when(orcamentoRepository.countByEmpresa_IdAndStatus(EMPRESA_ID, StatusOrcamento.APROVADO)).thenReturn(0L);

        when(orcamentoRepository.somarValorTotalPorStatus(EMPRESA_ID, StatusOrcamento.APROVADO)).thenReturn(BigDecimal.ZERO);

        DashboardFinanceiroResponseDTO financeiro = dashboardService.obterFinanceiro();

        assertEquals(BigDecimal.ZERO, financeiro.ticketMedio());

        assertEquals(0L, financeiro.quantidadeOrcamentosAprovados());
    }

    @Test
    void deveCalcularTicketMedioComOrcamentosAprovados() {

        prepararEmpresaAtual();

        when(pagamentoRepository.somarValorPorStatus(EMPRESA_ID, StatusPagamento.CONFIRMADO)).thenReturn(new BigDecimal("1000.00"));

        when(pagamentoRepository.somarValorPorStatus(EMPRESA_ID, StatusPagamento.PENDENTE)).thenReturn(new BigDecimal("200.00"));

        when(orcamentoRepository.countByEmpresa_IdAndStatus(EMPRESA_ID, StatusOrcamento.APROVADO)).thenReturn(2L);

        when(orcamentoRepository.somarValorTotalPorStatus(EMPRESA_ID, StatusOrcamento.APROVADO)).thenReturn(new BigDecimal("1000.00"));

        DashboardFinanceiroResponseDTO financeiro = dashboardService.obterFinanceiro();

        assertEquals(new BigDecimal("1000.00"), financeiro.valorRecebido());

        assertEquals(new BigDecimal("200.00"), financeiro.valorPendente());

        assertEquals(new BigDecimal("500.00"), financeiro.ticketMedio());

        assertEquals(2L, financeiro.quantidadeOrcamentosAprovados());
    }

    @Test
    void deveMontarOperacionalComContagensPorStatusEListasReutilizadas() {

        prepararEmpresaAtual();

        for (StatusOrdemServico status : StatusOrdemServico.values()) {

            when(ordemServicoRepository.countByEmpresa_IdAndStatus(EMPRESA_ID, status)).thenReturn(0L);
        }

        when(ordemServicoRepository.countByEmpresa_IdAndStatus(EMPRESA_ID, StatusOrdemServico.CONCLUIDA)).thenReturn(7L);

        when(ordemServicoRepository.countByEmpresa_IdAndStatus(EMPRESA_ID, StatusOrdemServico.CANCELADA)).thenReturn(2L);

        AgendamentoResponseDTO agendamento = new AgendamentoResponseDTO(1L, 1L, 2L, "Técnico", null, null, null, null, null);

        when(agendamentoService.listarProximos(7)).thenReturn(List.of(agendamento));

        PlanoManutencaoPreventivaResponseDTO plano = new PlanoManutencaoPreventivaResponseDTO(1L, 1L, "Marca", "Modelo", null, null, 6, null, null, true, null, null);

        when(planoManutencaoPreventivaService.listarProximas(30)).thenReturn(List.of(plano));

        DashboardOperacionalResponseDTO operacional = dashboardService.obterOperacional();

        assertEquals(7L, operacional.osConcluidas());

        assertEquals(2L, operacional.osCanceladas());

        assertEquals(StatusOrdemServico.values().length, operacional.osPorStatus().size());

        assertEquals(1, operacional.proximosAgendamentos().size());

        assertEquals(1, operacional.manutencoesPreventivasProximas().size());
    }
}
