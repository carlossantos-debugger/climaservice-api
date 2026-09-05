import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models/role.model';
import { AuthService } from '../services/auth.service';

/**
 * Bloqueia uma rota para quem não tem um dos perfis informados. Só faz sentido para
 * rotas que o backend também restringe de verdade (ex.: `/usuarios/**` é ADMIN-only em
 * `SecurityConfig`) — isso aqui é conforto visual, não é o mecanismo de segurança.
 *
 * TODO(feature/ux-hardening): redirecionar para uma página de "acesso negado" dedicada
 * em vez do dashboard.
 */
export function roleGuard(...allowedRoles: Role[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.hasRole(...allowedRoles) ? true : router.createUrlTree(['/dashboard']);
  };
}
