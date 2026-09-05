import { ChangeDetectionStrategy, Component, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface NavItem {
  path: string;
  label: string;
  icon: string;
}

const NAV_ITEMS: NavItem[] = [
  { path: 'dashboard', label: 'Dashboard', icon: 'dashboard' },
  { path: 'clientes', label: 'Clientes', icon: 'groups' },
  { path: 'equipamentos', label: 'Equipamentos', icon: 'ac_unit' },
  { path: 'ordens-servico', label: 'Ordens de Serviço', icon: 'build' },
  { path: 'agendamentos', label: 'Agendamentos', icon: 'event' },
  { path: 'manutencoes-preventivas', label: 'Manutenção Preventiva', icon: 'event_repeat' },
  { path: 'servicos', label: 'Serviços', icon: 'design_services' },
  { path: 'produtos', label: 'Produtos', icon: 'inventory_2' },
  { path: 'orcamentos', label: 'Orçamentos', icon: 'request_quote' },
  { path: 'pagamentos', label: 'Pagamentos', icon: 'payments' },
  { path: 'usuarios', label: 'Usuários', icon: 'manage_accounts' },
  { path: 'empresa', label: 'Empresa', icon: 'apartment' }
];

@Component({
  selector: 'app-sidebar',
  imports: [MatListModule, MatIconModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Sidebar {
  readonly navItems = NAV_ITEMS;

  /** Emitido ao clicar em um item — o layout usa isso para fechar o drawer no modo "over" (tablet). */
  readonly navigated = output<void>();
}
