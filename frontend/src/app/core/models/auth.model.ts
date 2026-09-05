import { Role } from './role.model';

/** Espelha LoginRequestDTO.java. */
export interface LoginRequest {
  email: string;
  senha: string;
}

/** Espelha LoginResponseDTO.java. */
export interface LoginResponse {
  usuarioId: number;
  nome: string;
  email: string;
  role: Role;
  token: string;
}

/** Subconjunto de LoginResponse que é seguro manter em memória/localStorage — sem o token. */
export interface AuthenticatedUser {
  id: number;
  nome: string;
  email: string;
  role: Role;
}
