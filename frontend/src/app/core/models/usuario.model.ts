import { Role } from './role.model';

/** Espelha UsuarioResponseDTO.java. */
export interface Usuario {
  id: number;
  nome: string;
  email: string;
  role: Role;
  ativo: boolean;
}

/**
 * Espelha UsuarioCadastroRequestDTO.java. Não existe DTO de atualização — o backend só
 * permite cadastrar, ativar e inativar, não editar nome/e-mail/role depois de criado.
 */
export interface UsuarioCadastroRequest {
  nome: string;
  email: string;
  senha: string;
  role: Role;
}

/**
 * Espelha TecnicoResumoDTO.java (GET /tecnicos) — só id+nome dos técnicos ativos da empresa,
 * aberto a ADMIN/ATENDENTE/TECNICO. Diferente de GET /usuarios (que devolve todos os perfis,
 * mas é ADMIN-only): use este para qualquer seletor de técnico em formulários.
 */
export interface TecnicoResumo {
  id: number;
  nome: string;
}
