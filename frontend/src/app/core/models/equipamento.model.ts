/** Espelha StatusEquipamento.java. */
export type StatusEquipamento = 'ATIVO' | 'INATIVO';

/** Espelha EquipamentoRequestDTO.java. */
export interface EquipamentoRequest {
  marca: string;
  modelo: string;
  capacidadeBtu: number;
  numeroSerie?: string;
  localInstalacao?: string;
  clienteId: number;
}

/** Espelha EquipamentoResponseDTO.java. */
export interface EquipamentoResponse {
  id: number;
  marca: string;
  modelo: string;
  capacidadeBtu: number;
  numeroSerie?: string;
  localInstalacao?: string;
  status: StatusEquipamento;
  clienteId: number;
  clienteNome: string;
}

/** Filtros de GET /equipamentos — todos opcionais, combináveis. */
export interface EquipamentoFiltro {
  clienteId?: number;
  status?: StatusEquipamento;
  marca?: string;
  modelo?: string;
}
