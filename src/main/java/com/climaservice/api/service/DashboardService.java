package com.climaservice.api.service;

import com.climaservice.api.dto.AgendamentoResponseDTO;
import com.climaservice.api.dto.DashboardFinanceiroResponseDTO;
import com.climaservice.api.dto.DashboardOperacionalResponseDTO;
import com.climaservice.api.dto.DashboardResumoResponseDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaResponseDTO;
import com.climaservice.api.entity.StatusAgendamento;
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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private static final int DIAS_PROXIMOS_AGENDAMENTOS = 7;
    private static final int DIAS_MANUTENCOES_PREVENTIVAS_PROXIMAS = 30;

    private final ClienteRepository clienteRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoService agendamentoService;
    private final PlanoManutencaoPreventivaService planoManutencaoPreventivaService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public DashboardService(ClienteRepository clienteRepository, EquipamentoRepository equipamentoRepository, OrdemServicoRepository ordemServicoRepository, OrcamentoRepository orcamentoRepository, PagamentoRepository pagamentoRepository, AgendamentoRepository agendamentoRepository, AgendamentoService agendamentoService, PlanoManutencaoPreventivaService planoManutencaoPreventivaService, UsuarioAutenticadoService usuarioAutenticadoService) {

        this.clienteRepository = clienteRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoService = agendamentoService;
        this.planoManutencaoPreventivaService = planoManutencaoPreventivaService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    /*
     * Cliente não possui campo de status/ativo (ver README) — "clientesAtivos"
     * é interpretado como o total de clientes cadastrados no tenant.
     */
    @Transactional(readOnly = true)
    public DashboardResumoResponseDTO obterResumo() {

        Long empresaId = obterEmpresaIdAtual();

        LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();

        LocalDateTime fimHoje = inicioHoje.plusDays(1).minusNanos(1);

        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        LocalDateTime fimMes = inicioMes.plusMonths(1).minusNanos(1);

        long clientesAtivos = clienteRepository.countByEmpresa_Id(empresaId);

        long equipamentosAtivos = equipamentoRepository.countByEmpresa_IdAndStatus(empresaId, StatusEquipamento.ATIVO);

        long ordensAbertas = ordemServicoRepository.countByEmpresa_IdAndStatus(empresaId, StatusOrdemServico.ABERTA);

        long ordensEmAndamento = ordemServicoRepository.countByEmpresa_IdAndStatus(empresaId, StatusOrdemServico.EM_ANDAMENTO);

        /*
         * Orcamento não possui um status PENDENTE literal — ENVIADO
         * representa o orçamento aguardando decisão do cliente.
         * RASCUNHO ainda não foi enviado, portanto não é "pendente".
         */
        long orcamentosPendentes = orcamentoRepository.countByEmpresa_IdAndStatus(empresaId, StatusOrcamento.ENVIADO);

        long agendamentosHoje = agendamentoRepository.countByEmpresa_IdAndDataHoraInicioBetweenAndStatusNot(empresaId, inicioHoje, fimHoje, StatusAgendamento.CANCELADO);

        BigDecimal receitaConfirmadaNoMes = pagamentoRepository.somarValorPorStatusEPeriodoConfirmacao(empresaId, StatusPagamento.CONFIRMADO, inicioMes, fimMes);

        return new DashboardResumoResponseDTO(clientesAtivos, equipamentosAtivos, ordensAbertas, ordensEmAndamento, orcamentosPendentes, agendamentosHoje, receitaConfirmadaNoMes);
    }

    @Transactional(readOnly = true)
    public DashboardFinanceiroResponseDTO obterFinanceiro() {

        Long empresaId = obterEmpresaIdAtual();

        BigDecimal valorRecebido = pagamentoRepository.somarValorPorStatus(empresaId, StatusPagamento.CONFIRMADO);

        BigDecimal valorPendente = pagamentoRepository.somarValorPorStatus(empresaId, StatusPagamento.PENDENTE);

        long quantidadeOrcamentosAprovados = orcamentoRepository.countByEmpresa_IdAndStatus(empresaId, StatusOrcamento.APROVADO);

        BigDecimal somaValorAprovados = orcamentoRepository.somarValorTotalPorStatus(empresaId, StatusOrcamento.APROVADO);

        BigDecimal ticketMedio = quantidadeOrcamentosAprovados == 0 ? BigDecimal.ZERO : somaValorAprovados.divide(BigDecimal.valueOf(quantidadeOrcamentosAprovados), 2, RoundingMode.HALF_UP);

        return new DashboardFinanceiroResponseDTO(valorRecebido, valorPendente, ticketMedio, quantidadeOrcamentosAprovados);
    }

    @Transactional(readOnly = true)
    public DashboardOperacionalResponseDTO obterOperacional() {

        Long empresaId = obterEmpresaIdAtual();

        Map<StatusOrdemServico, Long> osPorStatus = new LinkedHashMap<>();

        for (StatusOrdemServico status : StatusOrdemServico.values()) {

            osPorStatus.put(status, ordemServicoRepository.countByEmpresa_IdAndStatus(empresaId, status));
        }

        long osConcluidas = osPorStatus.getOrDefault(StatusOrdemServico.CONCLUIDA, 0L);

        long osCanceladas = osPorStatus.getOrDefault(StatusOrdemServico.CANCELADA, 0L);

        List<AgendamentoResponseDTO> proximosAgendamentos = agendamentoService.listarProximos(DIAS_PROXIMOS_AGENDAMENTOS);

        List<PlanoManutencaoPreventivaResponseDTO> manutencoesPreventivasProximas = planoManutencaoPreventivaService.listarProximas(DIAS_MANUTENCOES_PREVENTIVAS_PROXIMAS);

        return new DashboardOperacionalResponseDTO(osPorStatus, osConcluidas, osCanceladas, proximosAgendamentos, manutencoesPreventivasProximas);
    }

    private Long obterEmpresaIdAtual() {

        return usuarioAutenticadoService.obterEmpresaAtual().getId();
    }
}
