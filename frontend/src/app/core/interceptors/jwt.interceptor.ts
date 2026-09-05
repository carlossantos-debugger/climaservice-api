import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Anexa `Authorization: Bearer <token>` em toda requisição (quando há sessão) e trata
 * globalmente as respostas 401/403:
 * - 401 (token ausente/expirado/inválido): a sessão não serve mais — limpa e manda para /login.
 * - 403 (autenticado, mas sem permissão para aquela ação): avisa e deixa o erro seguir,
 *   para a tela que fez a chamada decidir o que mais fazer (ex.: manter o formulário preenchido).
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const snackBar = inject(MatSnackBar);

  const token = authService.token();
  const authReq = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(authReq).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 401) {
          authService.logout();
        } else if (error.status === 403) {
          snackBar.open('Você não tem permissão para essa ação.', 'Ok', { duration: 4000 });
        }
      }
      return throwError(() => error);
    })
  );
};
