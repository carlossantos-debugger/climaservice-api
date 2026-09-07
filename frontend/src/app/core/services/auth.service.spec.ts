import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LoginResponse } from '../models/auth.model';
import { AuthService } from './auth.service';

const TOKEN_KEY = 'climaservice.token';
const USER_KEY = 'climaservice.user';

const LOGIN_RESPONSE: LoginResponse = {
  usuarioId: 7,
  nome: 'Ana Souza',
  email: 'ana@empresa.com',
  role: 'ATENDENTE',
  token: 'jwt-fake-token'
};

describe('AuthService', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('inicia sem sessão quando o localStorage está vazio', () => {
    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
    expect(service.token()).toBeNull();
  });

  it('carrega a sessão do localStorage ao iniciar, se houver uma salva', () => {
    localStorage.setItem(TOKEN_KEY, 'token-salvo');
    localStorage.setItem(
      USER_KEY,
      JSON.stringify({ id: 1, nome: 'Carlos', email: 'carlos@empresa.com', role: 'ADMIN' })
    );

    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()).toEqual({ id: 1, nome: 'Carlos', email: 'carlos@empresa.com', role: 'ADMIN' });
    expect(service.token()).toBe('token-salvo');
  });

  it('ignora um usuário corrompido no localStorage em vez de lançar erro', () => {
    localStorage.setItem(USER_KEY, '{json inválido');

    const service = TestBed.inject(AuthService);

    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('login() faz POST /auth/login, guarda a sessão e devolve o usuário sem o token', () => {
    const service = TestBed.inject(AuthService);
    const http = TestBed.inject(HttpTestingController);

    let resultado: unknown;
    service.login('ana@empresa.com', 'senha123').subscribe((usuario) => (resultado = usuario));

    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'ana@empresa.com', senha: 'senha123' });
    req.flush(LOGIN_RESPONSE);

    expect(resultado).toEqual({ id: 7, nome: 'Ana Souza', email: 'ana@empresa.com', role: 'ATENDENTE' });
    expect(service.isAuthenticated()).toBe(true);
    expect(service.token()).toBe('jwt-fake-token');
    expect(localStorage.getItem(TOKEN_KEY)).toBe('jwt-fake-token');
    expect(JSON.parse(localStorage.getItem(USER_KEY)!)).toEqual({
      id: 7,
      nome: 'Ana Souza',
      email: 'ana@empresa.com',
      role: 'ATENDENTE'
    });
  });

  it('logout() limpa a sessão e navega para /login', () => {
    localStorage.setItem(TOKEN_KEY, 'token-salvo');
    localStorage.setItem(USER_KEY, JSON.stringify({ id: 1, nome: 'Carlos', email: 'c@e.com', role: 'ADMIN' }));

    const service = TestBed.inject(AuthService);
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
    expect(localStorage.getItem(USER_KEY)).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('hasRole() reflete o perfil da sessão atual e é sempre falso sem sessão', () => {
    const service = TestBed.inject(AuthService);

    expect(service.hasRole('ADMIN', 'ATENDENTE')).toBe(false);

    service.login('ana@empresa.com', 'senha123').subscribe();
    TestBed.inject(HttpTestingController).expectOne(`${environment.apiUrl}/auth/login`).flush(LOGIN_RESPONSE);

    expect(service.hasRole('ATENDENTE')).toBe(true);
    expect(service.hasRole('ADMIN', 'TECNICO')).toBe(false);
  });
});
