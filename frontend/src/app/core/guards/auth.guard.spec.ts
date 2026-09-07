import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  let isAuthenticated: boolean;

  beforeEach(() => {
    isAuthenticated = false;

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { isAuthenticated: () => isAuthenticated }
        }
      ]
    });
  });

  it('permite a navegação quando há sessão', () => {
    isAuthenticated = true;

    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/dashboard' } as never)
    );

    expect(resultado).toBe(true);
  });

  it('redireciona para /login preservando a URL de destino quando não há sessão', () => {
    isAuthenticated = false;

    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/clientes/5/editar' } as never)
    ) as UrlTree;

    const router = TestBed.inject(Router);
    expect(router.serializeUrl(resultado)).toBe('/login?returnUrl=%2Fclientes%2F5%2Feditar');
  });
});
