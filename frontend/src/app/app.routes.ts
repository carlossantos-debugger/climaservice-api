import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';
import { Login } from './features/auth/login/login';
import { MainLayout } from './layout/main-layout/main-layout';

export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [guestGuard] },
  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard)
      },
      {
        path: 'clientes',
        loadComponent: () => import('./features/clientes/cliente-list/cliente-list').then((m) => m.ClienteList)
      },
      {
        path: 'clientes/novo',
        loadComponent: () => import('./features/clientes/cliente-form/cliente-form').then((m) => m.ClienteForm),
        canActivate: [roleGuard('ADMIN', 'ATENDENTE')]
      },
      {
        path: 'clientes/:id/editar',
        loadComponent: () => import('./features/clientes/cliente-form/cliente-form').then((m) => m.ClienteForm),
        canActivate: [roleGuard('ADMIN', 'ATENDENTE')]
      },
      {
        path: 'equipamentos',
        loadComponent: () =>
          import('./features/equipamentos/equipamento-list/equipamento-list').then((m) => m.EquipamentoList)
      },
      {
        path: 'equipamentos/novo',
        loadComponent: () =>
          import('./features/equipamentos/equipamento-form/equipamento-form').then((m) => m.EquipamentoForm),
        canActivate: [roleGuard('ADMIN', 'ATENDENTE')]
      },
      {
        path: 'equipamentos/:id/editar',
        loadComponent: () =>
          import('./features/equipamentos/equipamento-form/equipamento-form').then((m) => m.EquipamentoForm),
        canActivate: [roleGuard('ADMIN', 'ATENDENTE')]
      },
      {
        path: 'ordens-servico',
        loadComponent: () =>
          import('./shared/components/placeholder-page/placeholder-page').then((m) => m.PlaceholderPage),
        data: { title: 'Ordens de Serviço', icon: 'build' }
      },
      {
        path: 'agendamentos',
        loadComponent: () =>
          import('./shared/components/placeholder-page/placeholder-page').then((m) => m.PlaceholderPage),
        data: { title: 'Agendamentos', icon: 'event' }
      },
      {
        path: 'manutencoes-preventivas',
        loadComponent: () =>
          import('./shared/components/placeholder-page/placeholder-page').then((m) => m.PlaceholderPage),
        data: { title: 'Manutenção Preventiva', icon: 'event_repeat' }
      },
      {
        path: 'servicos',
        loadComponent: () =>
          import('./shared/components/placeholder-page/placeholder-page').then((m) => m.PlaceholderPage),
        data: { title: 'Serviços', icon: 'design_services' }
      },
      {
        path: 'produtos',
        loadComponent: () =>
          import('./shared/components/placeholder-page/placeholder-page').then((m) => m.PlaceholderPage),
        data: { title: 'Produtos', icon: 'inventory_2' }
      },
      {
        path: 'orcamentos',
        loadComponent: () =>
          import('./shared/components/placeholder-page/placeholder-page').then((m) => m.PlaceholderPage),
        data: { title: 'Orçamentos', icon: 'request_quote' }
      },
      {
        path: 'pagamentos',
        loadComponent: () =>
          import('./shared/components/placeholder-page/placeholder-page').then((m) => m.PlaceholderPage),
        data: { title: 'Pagamentos', icon: 'payments' }
      },
      {
        path: 'usuarios',
        loadComponent: () =>
          import('./shared/components/placeholder-page/placeholder-page').then((m) => m.PlaceholderPage),
        canActivate: [roleGuard('ADMIN')],
        data: { title: 'Usuários', icon: 'manage_accounts' }
      },
      {
        path: 'empresa',
        loadComponent: () =>
          import('./shared/components/placeholder-page/placeholder-page').then((m) => m.PlaceholderPage),
        data: { title: 'Empresa', icon: 'apartment' }
      }
    ]
  },
  // TODO(feature/ux-hardening): página 404 dedicada em vez deste redirect.
  { path: '**', redirectTo: '' }
];
