/** Espelha PlanoManutencaoPreventivaRequestDTO.java — usado só na criação. */
export interface PlanoManutencaoRequest {
  equipamentoId: number;
  tecnicoPadraoId?: number;
  intervaloMeses: number;
  proximaExecucao?: string;
  observacao?: string;
}

/**
 * Espelha PlanoManutencaoPreventivaAtualizarRequestDTO.java — usado na edição. Note que
 * não tem `equipamentoId`: o backend não permite trocar o equipamento de um plano depois
 * de criado, só o técnico padrão, o intervalo, a próxima execução e a observação.
 */
export interface PlanoManutencaoAtualizarRequest {
  tecnicoPadraoId?: number;
  intervaloMeses: number;
  proximaExecucao: string;
  observacao?: string;
}

/** Espelha PlanoManutencaoPreventivaResponseDTO.java. */
export interface PlanoManutencaoResponse {
  id: number;
  equipamentoId: number;
  equipamentoMarca: string;
  equipamentoModelo: string;
  tecnicoPadraoId?: number;
  tecnicoPadraoNome?: string;
  intervaloMeses: number;
  proximaExecucao?: string;
  ultimaExecucao?: string;
  ativo: boolean;
  observacao?: string;
  dataCriacao: string;
}

/** Espelha PlanoManutencaoPreventivaExecucaoResponseDTO.java. */
export interface PlanoManutencaoExecucao {
  id: number;
  planoId: number;
  ordemServicoId: number;
  dataReferencia: string;
  dataExecucao: string;
  usuarioId: number;
  usuarioNome: string;
}

/** Filtros de GET /planos-manutencao-preventiva — ambos opcionais, combináveis. */
export interface PlanoManutencaoFiltro {
  equipamentoId?: number;
  ativo?: boolean;
}
