import { ChangeDetectionStrategy, Component, computed, inject, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  path: string;
  label: string;
  icon: string;
  /** Só faz sentido marcar como admin-only o que o backend também restringe de verdade. */
  adminOnly?: boolean;
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
  { path: 'usuarios', label: 'Usuários', icon: 'manage_accounts', adminOnly: true },
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
  private readonly authService = inject(AuthService);

  /**
   * `/usuarios/**` é bloqueado a nível de rota para não-ADMIN em SecurityConfig — esconder
   * o link é só conforto visual, a segurança real já está garantida pelo backend.
   */
  readonly navItems = computed(() =>
    NAV_ITEMS.filter((item) => !item.adminOnly || this.authService.hasRole('ADMIN'))
  );

  /** Emitido ao clicar em um item — o layout usa isso para fechar o drawer no modo "over" (tablet). */
  readonly navigated = output<void>();
}
