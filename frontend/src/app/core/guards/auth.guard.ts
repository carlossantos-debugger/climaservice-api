import { CanActivateFn } from '@angular/router';

/**
 * Protege as rotas internas (layout principal).
 *
 * Ainda não há `AuthService`/token nesta branch (feature/frontend-foundation) — a
 * verificação real de sessão (token presente, redirecionamento para /login em caso
 * negativo) entra na feature/authentication. Por ora o guard só existe para que as
 * rotas já nasçam "protegidas" e a próxima branch precise apenas preencher a lógica
 * aqui dentro, sem tocar em app.routes.ts.
 */
export const authGuard: CanActivateFn = () => {
  return true;
};
