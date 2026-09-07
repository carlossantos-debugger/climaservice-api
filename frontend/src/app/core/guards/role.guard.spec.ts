import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { roleGuard } from './role.guard';

describe('roleGuard', () => {
  let userRole: 'ADMIN' | 'ATENDENTE' | 'TECNICO' | null;

  beforeEach(() => {
    userRole = null;

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            hasRole: (...roles: string[]) => !!userRole && roles.includes(userRole)
          }
        }
      ]
    });
  });

  it('permite a navegação quando o usuário tem um dos perfis exigidos', () => {
    userRole = 'ADMIN';

    const resultado = TestBed.runInInjectionContext(() => roleGuard('ADMIN', 'ATENDENTE')({} as never, {} as never));

    expect(resultado).toBe(true);
  });

  it('redireciona para /acesso-negado quando o usuário não tem nenhum dos perfis exigidos', () => {
    userRole = 'TECNICO';

    const resultado = TestBed.runInInjectionContext(() =>
      roleGuard('ADMIN', 'ATENDENTE')({} as never, {} as never)
    ) as UrlTree;

    const router = TestBed.inject(Router);
    expect(router.serializeUrl(resultado)).toBe('/acesso-negado');
  });
});
