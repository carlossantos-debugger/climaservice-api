/** Espelha EnderecoDTO.java. Todos os campos são opcionais no backend. */
export interface Endereco {
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
  cep?: string;
}

/** Espelha ClienteRequestDTO.java. */
export interface ClienteRequest {
  nome: string;
  cpfCnpj: string;
  telefone?: string;
  email?: string;
  endereco?: Endereco;
  inscricaoMunicipal?: string;
  inscricaoEstadual?: string;
}

/** Espelha ClienteResponseDTO.java. */
export interface ClienteResponse {
  id: number;
  nome: string;
  cpfCnpj: string;
  telefone?: string;
  email?: string;
  endereco?: Endereco;
  inscricaoMunicipal?: string;
  inscricaoEstadual?: string;
}

/** Filtros de GET /clientes — ambos opcionais, combináveis. */
export interface ClienteFiltro {
  nome?: string;
  cpfCnpj?: string;
}
