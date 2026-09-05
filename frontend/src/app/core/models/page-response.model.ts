/**
 * Espelha `PageResponseDTO<T>` do backend. Todos os endpoints de listagem principal
 * (clientes, equipamentos, ordens de serviço, orçamentos, pagamentos, agendamentos)
 * respondem nesse envelope em vez de uma lista simples.
 */
export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
