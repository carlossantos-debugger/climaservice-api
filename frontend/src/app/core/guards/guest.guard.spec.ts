import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { guestGuard } from './guest.guard';

describe('guestGuard', () => {
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

  it('permite acessar /login quando não há sessão', () => {
    isAuthenticated = false;

    const resultado = TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never));

    expect(resultado).toBe(true);
  });

  it('redireciona para /dashboard quando já há sessão', () => {
    isAuthenticated = true;

    const resultado = TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never)) as UrlTree;

    const router = TestBed.inject(Router);
    expect(router.serializeUrl(resultado)).toBe('/dashboard');
  });
});
