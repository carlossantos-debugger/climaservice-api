/** Espelha FormaPagamento.java. */
export type FormaPagamento = 'DINHEIRO' | 'PIX' | 'CARTAO_CREDITO' | 'CARTAO_DEBITO' | 'BOLETO' | 'TRANSFERENCIA';

/** Espelha StatusPagamento.java. */
export type StatusPagamento = 'PENDENTE' | 'CONFIRMADO' | 'CANCELADO';

/** Espelha PagamentoRequestDTO.java. */
export interface PagamentoRequest {
  valor: number;
  formaPagamento: FormaPagamento;
  observacao?: string;
}

/** Espelha PagamentoResponseDTO.java. */
export interface PagamentoResponse {
  id: number;
  orcamentoId: number;
  valor: number;
  formaPagamento: FormaPagamento;
  status: StatusPagamento;
  dataCriacao: string;
  dataConfirmacao?: string;
  dataCancelamento?: string;
  observacao?: string;
}

/** Espelha PagamentoResumoResponseDTO.java. */
export interface PagamentoResumo {
  orcamentoId: number;
  valorTotal: number;
  totalPago: number;
  totalPendente: number;
  saldoRestante: number;
  valorDisponivelParaNovoPagamento: number;
}

/** Espelha PagamentoHistoricoResponseDTO.java. */
export interface PagamentoHistorico {
  id: number;
  statusAnterior: StatusPagamento;
  statusNovo: StatusPagamento;
  dataAlteracao: string;
  usuarioId: number;
  usuarioNome: string;
}

/** Filtros de GET /pagamentos — todos opcionais, combináveis. */
export interface PagamentoFiltro {
  status?: StatusPagamento;
  formaPagamento?: FormaPagamento;
  dataInicial?: string;
  dataFinal?: string;
}
