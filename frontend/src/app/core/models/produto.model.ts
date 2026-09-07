/** Espelha ProdutoRequestDTO.java. */
export interface ProdutoRequest {
  nome: string;
  descricao?: string;
  valorPadrao: number;
}

/** Espelha ProdutoResponseDTO.java. */
export interface ProdutoResponse {
  id: number;
  nome: string;
  descricao?: string;
  valorPadrao: number;
  ativo: boolean;
}
