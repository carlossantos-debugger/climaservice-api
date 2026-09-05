import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthenticatedUser, LoginRequest, LoginResponse } from '../models/auth.model';
import { Role } from '../models/role.model';

const TOKEN_KEY = 'climaservice.token';
const USER_KEY = 'climaservice.user';

function readStoredUser(): AuthenticatedUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as AuthenticatedUser) : null;
  } catch {
    return null;
  }
}

function toAuthenticatedUser(response: LoginResponse): AuthenticatedUser {
  return { id: response.usuarioId, nome: response.nome, email: response.email, role: response.role };
}

/**
 * Sessão do usuário autenticado. Guarda apenas o que o backend já devolveu no login —
 * nunca decodifica o JWT para tirar conclusões de segurança, o backend é a fonte de
 * verdade sobre quem pode fazer o quê.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly currentUserSignal = signal<AuthenticatedUser | null>(readStoredUser());
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  /** Lido de forma síncrona pelo jwtInterceptor a cada requisição. */
  token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  login(email: string, senha: string): Observable<AuthenticatedUser> {
    const body: LoginRequest = { email, senha };

    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, body).pipe(
      tap((response) => this.setSession(response)),
      map((response) => toAuthenticatedUser(response))
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSignal.set(null);
    this.router.navigate(['/login']);
  }

  /** Uso apenas visual (esconder/mostrar controles) — a autorização real é do backend. */
  hasRole(...roles: Role[]): boolean {
    const user = this.currentUserSignal();
    return !!user && roles.includes(user.role);
  }

  private setSession(response: LoginResponse): void {
    const user = toAuthenticatedUser(response);
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }
}
