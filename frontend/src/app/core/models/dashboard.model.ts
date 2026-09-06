/**
 * Espelha StatusOrdemServico.java. Definido aqui pois a feature `ordens-servico`
 * (branch própria) ainda não existe — quando ela chegar, mova este tipo para o
 * model dela e importe daqui.
 */
export type StatusOrdemServico = 'ABERTA' | 'EM_ANDAMENTO' | 'AGUARDANDO_CLIENTE' | 'CONCLUIDA' | 'CANCELADA';

/** Espelha StatusAgendamento.java — mesmo motivo do tipo acima. */
export type StatusAgendamento = 'AGENDADO' | 'CONFIRMADO' | 'EM_ATENDIMENTO' | 'CONCLUIDO' | 'CANCELADO';

/** Espelha DashboardResumoResponseDTO.java. */
export interface DashboardResumo {
  clientesAtivos: number;
  equipamentosAtivos: number;
  ordensAbertas: number;
  ordensEmAndamento: number;
  orcamentosPendentes: number;
  agendamentosHoje: number;
  receitaConfirmadaNoMes: number;
}

/** Espelha DashboardFinanceiroResponseDTO.java. */
export interface DashboardFinanceiro {
  valorRecebido: number;
  valorPendente: number;
  ticketMedio: number;
  quantidadeOrcamentosAprovados: number;
}

/** Espelha AgendamentoResponseDTO.java, no subconjunto usado pelo dashboard. */
export interface DashboardAgendamento {
  id: number;
  ordemServicoId: number;
  tecnicoNome: string;
  dataHoraInicio: string;
  dataHoraFim: string;
  status: StatusAgendamento;
}

/** Espelha PlanoManutencaoPreventivaResponseDTO.java, no subconjunto usado pelo dashboard. */
export interface DashboardManutencaoPreventiva {
  id: number;
  equipamentoMarca: string;
  equipamentoModelo: string;
  proximaExecucao: string;
}

/** Espelha DashboardOperacionalResponseDTO.java. */
export interface DashboardOperacional {
  osPorStatus: Partial<Record<StatusOrdemServico, number>>;
  osConcluidas: number;
  osCanceladas: number;
  proximosAgendamentos: DashboardAgendamento[];
  manutencoesPreventivasProximas: DashboardManutencaoPreventiva[];
}
