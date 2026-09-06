/** Espelha StatusAgendamento.java. */
export type StatusAgendamento = 'AGENDADO' | 'CONFIRMADO' | 'EM_ATENDIMENTO' | 'CONCLUIDO' | 'CANCELADO';

/**
 * Espelha o fluxo de status documentado no README (# Agenda de Atendimentos > Fluxo de
 * status) — conforto visual; a validação real das transições é do AgendamentoService.java.
 */
export const TRANSICOES_STATUS_AGENDAMENTO: Record<StatusAgendamento, StatusAgendamento[]> = {
  AGENDADO: ['CONFIRMADO', 'CANCELADO'],
  CONFIRMADO: ['EM_ATENDIMENTO', 'CANCELADO'],
  EM_ATENDIMENTO: ['CONCLUIDO', 'CANCELADO'],
  CONCLUIDO: [],
  CANCELADO: []
};

/** Espelha AgendamentoRequestDTO.java. */
export interface AgendamentoRequest {
  ordemServicoId: number;
  tecnicoId: number;
  dataHoraInicio: string;
  dataHoraFim: string;
  observacao?: string;
}

/** Espelha AgendamentoReagendarRequestDTO.java. */
export interface AgendamentoReagendarRequest {
  dataHoraInicio: string;
  dataHoraFim: string;
}

/** Espelha AgendamentoResponseDTO.java. */
export interface AgendamentoResponse {
  id: number;
  ordemServicoId: number;
  tecnicoId: number;
  tecnicoNome: string;
  dataHoraInicio: string;
  dataHoraFim: string;
  status: StatusAgendamento;
  observacao?: string;
  dataCriacao: string;
}

/** Espelha AgendamentoHistoricoResponseDTO.java. */
export interface AgendamentoHistorico {
  id: number;
  statusAnterior: StatusAgendamento;
  statusNovo: StatusAgendamento;
  dataAlteracao: string;
  usuarioId: number;
  usuarioNome: string;
}

/** Filtros de GET /agendamentos — todos opcionais, combináveis. */
export interface AgendamentoFiltro {
  dataInicial?: string;
  dataFinal?: string;
  tecnicoId?: number;
  status?: StatusAgendamento;
}
