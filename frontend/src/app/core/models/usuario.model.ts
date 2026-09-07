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
