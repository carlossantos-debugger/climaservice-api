/** Espelha ServicoRequestDTO.java. */
export interface ServicoRequest {
  nome: string;
  descricao?: string;
  valorPadrao: number;
}

/** Espelha ServicoResponseDTO.java. */
export interface ServicoResponse {
  id: number;
  nome: string;
  descricao?: string;
  valorPadrao: number;
  ativo: boolean;
}
