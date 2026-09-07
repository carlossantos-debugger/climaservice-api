/** Espelha StatusOrcamento.java. */
export type StatusOrcamento = 'RASCUNHO' | 'ENVIADO' | 'APROVADO' | 'REJEITADO' | 'CANCELADO';

/** Espelha TipoItemOrcamento.java. */
export type TipoItemOrcamento = 'SERVICO' | 'PECA';

/**
 * Espelha o workflow documentado no README (# Orçamentos > Workflow) — conforto visual;
 * a validação real das transições é do OrcamentoService.java.
 */
export const TRANSICOES_STATUS_ORCAMENTO: Record<StatusOrcamento, StatusOrcamento[]> = {
  RASCUNHO: ['ENVIADO', 'CANCELADO'],
  ENVIADO: ['APROVADO', 'REJEITADO'],
  APROVADO: [],
  REJEITADO: [],
  CANCELADO: []
};

/** Espelha OrcamentoRequestDTO.java. */
export interface OrcamentoRequest {
  observacao?: string;
}

/** Espelha OrcamentoResponseDTO.java. */
export interface OrcamentoResponse {
  id: number;
  ordemServicoId: number;
  status: StatusOrcamento;
  valorTotal: number;
  dataCriacao: string;
  dataEnvio?: string;
  dataResposta?: string;
  observacao?: string;
}

/** Espelha OrcamentoItemRequestDTO.java (POST .../itens/servicos). */
export interface OrcamentoItemServicoRequest {
  servicoId: number;
  quantidade: number;
  valorUnitario?: number;
}

/** Espelha OrcamentoProdutoItemRequestDTO.java (POST .../itens/produtos). */
export interface OrcamentoItemProdutoRequest {
  produtoId: number;
  quantidade: number;
  valorUnitario?: number;
}

/** Espelha AtualizarOrcamentoItemRequestDTO.java. */
export interface OrcamentoItemAtualizarRequest {
  quantidade: number;
  valorUnitario?: number;
}

/** Espelha OrcamentoItemResponseDTO.java. */
export interface OrcamentoItemResponse {
  id: number;
  tipo: TipoItemOrcamento;
  servicoId?: number;
  produtoId?: number;
  descricao: string;
  quantidade: number;
  valorUnitario: number;
  subtotal: number;
}

/** Espelha OrcamentoHistoricoResponseDTO.java. */
export interface OrcamentoHistorico {
  id: number;
  statusAnterior: StatusOrcamento;
  statusNovo: StatusOrcamento;
  dataAlteracao: string;
  usuarioId: number;
  usuarioNome: string;
}

/** Filtros de GET /orcamentos — ambos opcionais, combináveis. */
export interface OrcamentoFiltro {
  status?: StatusOrcamento;
  dataInicial?: string;
  dataFinal?: string;
}
