/** Espelha StatusOrdemServico.java. */
export type StatusOrdemServico = 'ABERTA' | 'EM_ANDAMENTO' | 'AGUARDANDO_CLIENTE' | 'CONCLUIDA' | 'CANCELADA';

/**
 * Espelha o fluxo de status documentado no README (# Ordens de Serviço > Fluxo de status) —
 * é só conforto visual (esconder transições que o backend rejeitaria); a validação real das
 * transições acontece em OrdemServicoService.java.
 */
export const TRANSICOES_STATUS_OS: Record<StatusOrdemServico, StatusOrdemServico[]> = {
  ABERTA: ['EM_ANDAMENTO', 'CANCELADA'],
  EM_ANDAMENTO: ['AGUARDANDO_CLIENTE', 'CONCLUIDA', 'CANCELADA'],
  AGUARDANDO_CLIENTE: ['EM_ANDAMENTO', 'CANCELADA'],
  CONCLUIDA: [],
  CANCELADA: []
};

/** Espelha OrdemServicoRequestDTO.java. */
export interface OrdemServicoRequest {
  clienteId: number;
  equipamentoId: number;
  descricaoProblema: string;
}

/** Espelha OrdemServicoResponseDTO.java. */
export interface OrdemServicoResponse {
  id: number;
  clienteId: number;
  clienteNome: string;
  equipamentoId: number;
  equipamentoMarca: string;
  equipamentoModelo: string;
  descricaoProblema: string;
  diagnostico?: string;
  status: StatusOrdemServico;
  dataAbertura: string;
  dataConclusao?: string;
}

/** Espelha OrdemServicoHistoricoResponseDTO.java. */
export interface OrdemServicoHistorico {
  id: number;
  statusAnterior: StatusOrdemServico;
  statusNovo: StatusOrdemServico;
  dataAlteracao: string;
  usuarioId: number;
  usuarioNome: string;
}

/** Espelha OrdemServicoDiagnosticoHistoricoResponseDTO.java. */
export interface OrdemServicoDiagnosticoHistorico {
  id: number;
  diagnosticoAnterior?: string;
  diagnosticoNovo: string;
  dataAlteracao: string;
  usuarioId: number;
  usuarioNome: string;
}

/** Filtros de GET /ordens-servico — todos opcionais, combináveis. */
export interface OrdemServicoFiltro {
  status?: StatusOrdemServico;
  clienteId?: number;
  equipamentoId?: number;
  dataInicial?: string;
  dataFinal?: string;
}
