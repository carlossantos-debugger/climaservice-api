import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { Observable, Subject, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { AuthenticatedUser } from '../../../core/models/auth.model';
import { Login } from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let component: Login;
  let authService: { login: ReturnType<typeof vi.fn> };
  let router: Router;

  function configurar(returnUrl: string | null = null) {
    TestBed.resetTestingModule();
    authService = { login: vi.fn() };

    TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap(returnUrl ? { returnUrl } : {}) } }
        }
      ]
    });

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  }

  beforeEach(() => configurar());

  it('não chama login() e marca os campos como tocados quando o formulário é inválido', () => {
    component.submit();

    expect(authService.login).not.toHaveBeenCalled();
    expect(component.form.controls.email.touched).toBe(true);
    expect(component.form.controls.senha.touched).toBe(true);
  });

  it('rejeita um e-mail em formato inválido', () => {
    component.form.setValue({ email: 'não-é-email', senha: 'qualquer' });
    expect(component.form.controls.email.invalid).toBe(true);
  });

  it('em sucesso, navega para /dashboard quando não há returnUrl', () => {
    authService.login.mockReturnValue(new Observable<AuthenticatedUser>((sub) => sub.next({} as AuthenticatedUser)));
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockImplementation(() => Promise.resolve(true));

    component.form.setValue({ email: 'user@empresa.com', senha: 'senha123' });
    component.submit();

    expect(authService.login).toHaveBeenCalledWith('user@empresa.com', 'senha123');
    expect(navigateSpy).toHaveBeenCalledWith('/dashboard');
  });

  it('em sucesso, navega para o returnUrl da query string quando presente', () => {
    configurar('/clientes/5/editar');
    authService.login.mockReturnValue(new Observable<AuthenticatedUser>((sub) => sub.next({} as AuthenticatedUser)));
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockImplementation(() => Promise.resolve(true));

    component.form.setValue({ email: 'user@empresa.com', senha: 'senha123' });
    component.submit();

    expect(navigateSpy).toHaveBeenCalledWith('/clientes/5/editar');
  });

  it('em erro, exibe a mensagem e libera o formulário para nova tentativa', () => {
    authService.login.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 401, error: { message: 'Credenciais inválidas' } }))
    );

    component.form.setValue({ email: 'user@empresa.com', senha: 'errada' });
    component.submit();

    expect(component.submitting()).toBe(false);
    expect(component.errorMessage()).toBe('Credenciais inválidas');
  });

  it('ignora um segundo submit enquanto o primeiro ainda está em andamento', () => {
    const emissor = new Subject<AuthenticatedUser>();
    authService.login.mockReturnValue(emissor.asObservable());

    component.form.setValue({ email: 'user@empresa.com', senha: 'senha123' });
    component.submit();
    component.submit();

    expect(authService.login).toHaveBeenCalledTimes(1);
  });
});
