import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { MainLayout } from './layout/main-layout/main-layout';
import { PlaceholderPage } from './shared/components/placeholder-page/placeholder-page';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        component: PlaceholderPage,
        data: { title: 'Dashboard', icon: 'dashboard' }
      },
      {
        path: 'clientes',
        component: PlaceholderPage,
        data: { title: 'Clientes', icon: 'groups' }
      },
      {
        path: 'equipamentos',
        component: PlaceholderPage,
        data: { title: 'Equipamentos', icon: 'ac_unit' }
      },
      {
        path: 'ordens-servico',
        component: PlaceholderPage,
        data: { title: 'Ordens de Serviço', icon: 'build' }
      },
      {
        path: 'agendamentos',
        component: PlaceholderPage,
        data: { title: 'Agendamentos', icon: 'event' }
      },
      {
        path: 'manutencoes-preventivas',
        component: PlaceholderPage,
        data: { title: 'Manutenção Preventiva', icon: 'event_repeat' }
      },
      {
        path: 'servicos',
        component: PlaceholderPage,
        data: { title: 'Serviços', icon: 'design_services' }
      },
      {
        path: 'produtos',
        component: PlaceholderPage,
        data: { title: 'Produtos', icon: 'inventory_2' }
      },
      {
        path: 'orcamentos',
        component: PlaceholderPage,
        data: { title: 'Orçamentos', icon: 'request_quote' }
      },
      {
        path: 'pagamentos',
        component: PlaceholderPage,
        data: { title: 'Pagamentos', icon: 'payments' }
      },
      {
        path: 'usuarios',
        component: PlaceholderPage,
        data: { title: 'Usuários', icon: 'manage_accounts' }
      },
      {
        path: 'empresa',
        component: PlaceholderPage,
        data: { title: 'Empresa', icon: 'apartment' }
      }
    ]
  },
  // TODO(feature/authentication): rota pública /login.
  // TODO(feature/ux-hardening): página 404 dedicada em vez deste redirect.
  { path: '**', redirectTo: '' }
];
