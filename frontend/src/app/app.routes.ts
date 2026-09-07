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
          import('./features/ordens-servico/ordem-servico-list/ordem-servico-list').then((m) => m.OrdemServicoList)
      },
      {
        path: 'ordens-servico/nova',
        loadComponent: () =>
          import('./features/ordens-servico/ordem-servico-form/ordem-servico-form').then((m) => m.OrdemServicoForm),
        canActivate: [roleGuard('ADMIN', 'TECNICO')]
      },
      {
        path: 'ordens-servico/:id',
        loadComponent: () =>
          import('./features/ordens-servico/ordem-servico-detail/ordem-servico-detail').then(
            (m) => m.OrdemServicoDetail
          )
      },
      {
        path: 'agendamentos',
        loadComponent: () =>
          import('./features/agendamentos/agendamento-list/agendamento-list').then((m) => m.AgendamentoList)
      },
      {
        path: 'agendamentos/novo',
        loadComponent: () =>
          import('./features/agendamentos/agendamento-form/agendamento-form').then((m) => m.AgendamentoForm),
        canActivate: [roleGuard('ADMIN', 'ATENDENTE')]
      },
      {
        path: 'agendamentos/:id',
        loadComponent: () =>
          import('./features/agendamentos/agendamento-detail/agendamento-detail').then((m) => m.AgendamentoDetail)
      },
      {
        path: 'manutencoes-preventivas',
        loadComponent: () => import('./features/manutencoes-preventivas/plano-list/plano-list').then((m) => m.PlanoList)
      },
      {
        path: 'manutencoes-preventivas/novo',
        loadComponent: () =>
          import('./features/manutencoes-preventivas/plano-form/plano-form').then((m) => m.PlanoForm),
        canActivate: [roleGuard('ADMIN', 'ATENDENTE')]
      },
      {
        path: 'manutencoes-preventivas/:id/editar',
        loadComponent: () =>
          import('./features/manutencoes-preventivas/plano-form/plano-form').then((m) => m.PlanoForm),
        canActivate: [roleGuard('ADMIN', 'ATENDENTE')]
      },
      {
        path: 'manutencoes-preventivas/:id/execucoes',
        loadComponent: () =>
          import('./features/manutencoes-preventivas/plano-execucoes/plano-execucoes').then((m) => m.PlanoExecucoes)
      },
      {
        path: 'servicos',
        loadComponent: () => import('./features/servicos/servico-list/servico-list').then((m) => m.ServicoList)
      },
      {
        path: 'servicos/novo',
        loadComponent: () => import('./features/servicos/servico-form/servico-form').then((m) => m.ServicoForm),
        canActivate: [roleGuard('ADMIN', 'ATENDENTE')]
      },
      {
        path: 'servicos/:id/editar',
        loadComponent: () => import('./features/servicos/servico-form/servico-form').then((m) => m.ServicoForm),
        canActivate: [roleGuard('ADMIN', 'ATENDENTE')]
      },
      {
        path: 'produtos',
        loadComponent: () => import('./features/produtos/produto-list/produto-list').then((m) => m.ProdutoList)
      },
      {
        path: 'produtos/novo',
        loadComponent: () => import('./features/produtos/produto-form/produto-form').then((m) => m.ProdutoForm),
        canActivate: [roleGuard('ADMIN', 'ATENDENTE')]
      },
      {
        path: 'produtos/:id/editar',
        loadComponent: () => import('./features/produtos/produto-form/produto-form').then((m) => m.ProdutoForm),
        canActivate: [roleGuard('ADMIN', 'ATENDENTE')]
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
