import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth.service';
import { jwtInterceptor } from './jwt.interceptor';

describe('jwtInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: { token: ReturnType<typeof vi.fn>; logout: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authService = { token: vi.fn(() => null), logout: vi.fn() };
    snackBar = { open: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('anexa o header Authorization quando há token', () => {
    authService.token.mockReturnValue('abc123');

    http.get('/qualquer').subscribe();

    const req = httpMock.expectOne('/qualquer');
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc123');
    req.flush({});
  });

  it('não anexa Authorization quando não há sessão', () => {
    http.get('/qualquer').subscribe();

    const req = httpMock.expectOne('/qualquer');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('em 401 de um endpoint qualquer, encerra a sessão', () => {
    http.get('/orcamentos').subscribe({ error: () => undefined });

    httpMock.expectOne('/orcamentos').flush('não autorizado', { status: 401, statusText: 'Unauthorized' });

    expect(authService.logout).toHaveBeenCalled();
  });

  it('em 401 do próprio /auth/login, não encerra sessão (é só credencial inválida)', () => {
    http.post('/auth/login', {}).subscribe({ error: () => undefined });

    httpMock.expectOne('/auth/login').flush('credenciais inválidas', { status: 401, statusText: 'Unauthorized' });

    expect(authService.logout).not.toHaveBeenCalled();
  });

  it('em 403, avisa via snackbar e deixa o erro seguir', () => {
    let erroRecebido: unknown;
    http.post('/usuarios', {}).subscribe({ error: (err) => (erroRecebido = err) });

    httpMock.expectOne('/usuarios').flush('sem permissão', { status: 403, statusText: 'Forbidden' });

    expect(snackBar.open).toHaveBeenCalledWith('Você não tem permissão para essa ação.', 'Ok', { duration: 4000 });
    expect(erroRecebido).toBeTruthy();
  });

  it('em status 0 (sem conexão), avisa via snackbar', () => {
    http.get('/dashboard/resumo').subscribe({ error: () => undefined });

    httpMock.expectOne('/dashboard/resumo').error(new ProgressEvent('error'), { status: 0, statusText: 'Unknown' });

    expect(snackBar.open).toHaveBeenCalledWith(
      'Não foi possível conectar ao servidor. Verifique sua conexão.',
      'Ok',
      { duration: 5000 }
    );
  });

  it('em erro 5xx, avisa via snackbar', () => {
    http.get('/clientes').subscribe({ error: () => undefined });

    httpMock.expectOne('/clientes').flush('erro interno', { status: 500, statusText: 'Internal Server Error' });

    expect(snackBar.open).toHaveBeenCalledWith(
      'O servidor encontrou um erro inesperado. Tente novamente em instantes.',
      'Ok',
      { duration: 5000 }
    );
  });

  it('em erro de negócio comum (400), não chama logout nem snackbar — a tela trata', () => {
    http.post('/clientes', {}).subscribe({ error: () => undefined });

    httpMock.expectOne('/clientes').flush('nome obrigatório', { status: 400, statusText: 'Bad Request' });

    expect(authService.logout).not.toHaveBeenCalled();
    expect(snackBar.open).not.toHaveBeenCalled();
  });
});
