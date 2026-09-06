import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { forkJoin } from 'rxjs';
import { DashboardService } from '../../core/services/dashboard.service';
import {
  DashboardFinanceiro,
  DashboardOperacional,
  DashboardResumo,
  StatusAgendamento,
  StatusOrdemServico
} from '../../core/models/dashboard.model';
import { ErrorMessage } from '../../shared/components/error-message/error-message';
import { Loading } from '../../shared/components/loading/loading';
import { extractErrorMessage } from '../../core/utils/api-error.util';

const STATUS_OS_LABEL: Record<StatusOrdemServico, string> = {
  ABERTA: 'Aberta',
  EM_ANDAMENTO: 'Em andamento',
  AGUARDANDO_CLIENTE: 'Aguardando cliente',
  CONCLUIDA: 'Concluída',
  CANCELADA: 'Cancelada'
};

const STATUS_AGENDAMENTO_LABEL: Record<StatusAgendamento, string> = {
  AGENDADO: 'Agendado',
  CONFIRMADO: 'Confirmado',
  EM_ATENDIMENTO: 'Em atendimento',
  CONCLUIDO: 'Concluído',
  CANCELADO: 'Cancelado'
};

@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, DatePipe, MatCardModule, MatChipsModule, MatIconModule, MatListModule, ErrorMessage, Loading],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Dashboard implements OnInit {
  private readonly dashboardService = inject(DashboardService);

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly resumo = signal<DashboardResumo | null>(null);
  readonly financeiro = signal<DashboardFinanceiro | null>(null);
  readonly operacional = signal<DashboardOperacional | null>(null);

  readonly statusOsLabel = STATUS_OS_LABEL;
  readonly statusAgendamentoLabel = STATUS_AGENDAMENTO_LABEL;

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      resumo: this.dashboardService.resumo(),
      financeiro: this.dashboardService.financeiro(),
      operacional: this.dashboardService.operacional()
    }).subscribe({
      next: ({ resumo, financeiro, operacional }) => {
        this.resumo.set(resumo);
        this.financeiro.set(financeiro);
        this.operacional.set(operacional);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar o dashboard.'));
        this.loading.set(false);
      }
    });
  }

  osPorStatusEntries(operacional: DashboardOperacional): { status: StatusOrdemServico; total: number }[] {
    return (Object.entries(operacional.osPorStatus) as [StatusOrdemServico, number][]).map(([status, total]) => ({
      status,
      total
    }));
  }
}
