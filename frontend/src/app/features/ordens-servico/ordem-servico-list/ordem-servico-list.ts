import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { provideNativeDateAdapter } from '@angular/material/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { OrdemServicoService } from '../../../core/services/ordem-servico.service';
import { OrdemServicoResponse, StatusOrdemServico } from '../../../core/models/ordem-servico.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

const STATUS_LABEL: Record<StatusOrdemServico, string> = {
  ABERTA: 'Aberta',
  EM_ANDAMENTO: 'Em andamento',
  AGUARDANDO_CLIENTE: 'Aguardando cliente',
  CONCLUIDA: 'Concluída',
  CANCELADA: 'Cancelada'
};

@Component({
  selector: 'app-ordem-servico-list',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    DatePipe,
    MatButtonModule,
    MatChipsModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    EmptyState,
    ErrorMessage,
    Loading
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './ordem-servico-list.html',
  styleUrl: './ordem-servico-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrdemServicoList implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly ordemServicoService = inject(OrdemServicoService);
  private readonly authService = inject(AuthService);

  readonly statusLabel = STATUS_LABEL;
  readonly statusOptions: StatusOrdemServico[] = ['ABERTA', 'EM_ANDAMENTO', 'AGUARDANDO_CLIENTE', 'CONCLUIDA', 'CANCELADA'];

  readonly filtroForm = this.fb.group({
    status: this.fb.control<StatusOrdemServico | ''>(''),
    dataInicial: this.fb.control<Date | null>(null),
    dataFinal: this.fb.control<Date | null>(null)
  });

  readonly ordens = signal<OrdemServicoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly podeAbrirOs = computed(() => this.authService.hasRole('ADMIN', 'TECNICO'));

  ngOnInit(): void {
    this.carregar();
  }

  buscar(): void {
    this.pageIndex.set(0);
    this.carregar();
  }

  onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.carregar();
  }

  private carregar(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const { status, dataInicial, dataFinal } = this.filtroForm.getRawValue();

    this.ordemServicoService
      .listar(
        {
          status: status || undefined,
          dataInicial: dataInicial ? inicioDoDia(dataInicial) : undefined,
          dataFinal: dataFinal ? fimDoDia(dataFinal) : undefined
        },
        this.pageIndex(),
        this.pageSize()
      )
      .subscribe({
        next: (page) => {
          this.ordens.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar as ordens de serviço.'));
          this.loading.set(false);
        }
      });
  }
}

function inicioDoDia(data: Date): string {
  return `${dataParaISO(data)}T00:00:00`;
}

function fimDoDia(data: Date): string {
  return `${dataParaISO(data)}T23:59:59`;
}

function dataParaISO(data: Date): string {
  const ano = data.getFullYear();
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${ano}-${mes}-${dia}`;
}
