import { Endereco } from './cliente.model';

/** Espelha RegimeTributario.java. */
export type RegimeTributario = 'SIMPLES_NACIONAL' | 'LUCRO_PRESUMIDO' | 'LUCRO_REAL' | 'MEI';

/**
 * Espelha EmpresaAtualizarRequestDTO.java. Apesar do endpoint ser PATCH, o DTO exige o nome
 * (@NotBlank) — não é um patch parcial de verdade, o formulário deve sempre reenviar todos
 * os campos carregados do GET, não só o que mudou.
 */
export interface EmpresaAtualizarRequest {
  nome: string;
  cpfCnpj?: string;
  endereco?: Endereco;
  inscricaoMunicipal?: string;
  regimeTributario?: RegimeTributario;
  codigoServicoPadrao?: string;
  aliquotaIssPadrao?: number;
}

/** Espelha EmpresaResponseDTO.java. */
export interface EmpresaResponse {
  id: number;
  nome: string;
  cpfCnpj: string;
  ativo: boolean;
  dataCriacao: string;
  endereco?: Endereco;
  inscricaoMunicipal?: string;
  regimeTributario?: RegimeTributario;
  codigoServicoPadrao?: string;
  aliquotaIssPadrao?: number;
}
