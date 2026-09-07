import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Anexa `Authorization: Bearer <token>` em toda requisição (quando há sessão) e trata
 * globalmente as respostas de erro:
 * - 401 (token ausente/expirado/inválido): a sessão não serve mais — limpa e manda para /login.
 *   Exceção: um 401 do próprio `/auth/login` é só "credenciais inválidas", não sessão
 *   expirada — nesse caso o erro segue direto para o formulário, sem passar por logout().
 * - 403 (autenticado, mas sem permissão para aquela ação): avisa e deixa o erro seguir,
 *   para a tela que fez a chamada decidir o que mais fazer (ex.: manter o formulário preenchido).
 * - status 0 (sem conexão/CORS/backend fora do ar) ou 5xx: nenhuma tela trata isso de forma
 *   específica hoje (cada uma só sabe formatar erros de negócio 4xx da própria ação) — avisa
 *   aqui, uma vez, para toda a aplicação, e deixa o erro seguir do mesmo jeito.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const snackBar = inject(MatSnackBar);

  const token = authService.token();
  const authReq = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(authReq).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 401 && !req.url.endsWith('/auth/login')) {
          authService.logout();
        } else if (error.status === 403) {
          snackBar.open('Você não tem permissão para essa ação.', 'Ok', { duration: 4000 });
        } else if (error.status === 0) {
          snackBar.open('Não foi possível conectar ao servidor. Verifique sua conexão.', 'Ok', { duration: 5000 });
        } else if (error.status >= 500) {
          snackBar.open('O servidor encontrou um erro inesperado. Tente novamente em instantes.', 'Ok', {
            duration: 5000
          });
        }
      }
      return throwError(() => error);
    })
  );
};
