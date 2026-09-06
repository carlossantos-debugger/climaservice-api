import { Role } from './role.model';

/**
 * Espelha UsuarioResponseDTO.java. Só o necessário para o seletor de técnico em
 * Agendamentos por enquanto — a feature `usuarios` (branch própria, cadastro/ativar/
 * inativar) deve estender isso quando chegar.
 */
export interface Usuario {
  id: number;
  nome: string;
  email: string;
  role: Role;
  ativo: boolean;
}
