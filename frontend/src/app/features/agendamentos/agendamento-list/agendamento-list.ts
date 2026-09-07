import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { AgendamentoService } from '../../../core/services/agendamento.service';
import { AuthService } from '../../../core/services/auth.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { AgendamentoResponse, StatusAgendamento } from '../../../core/models/agendamento.model';
import { TecnicoResumo } from '../../../core/models/usuario.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { fimDoDia, inicioDoDia } from '../../../core/utils/date-filtro.util';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

const STATUS_LABEL: Record<StatusAgendamento, string> = {
  AGENDADO: 'Agendado',
  CONFIRMADO: 'Confirmado',
  EM_ATENDIMENTO: 'Em atendimento',
  CONCLUIDO: 'Concluído',
  CANCELADO: 'Cancelado'
};

@Component({
  selector: 'app-agendamento-list',
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
  templateUrl: './agendamento-list.html',
  styleUrl: './agendamento-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgendamentoList implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly agendamentoService = inject(AgendamentoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly authService = inject(AuthService);

  readonly statusLabel = STATUS_LABEL;
  readonly statusOptions: StatusAgendamento[] = ['AGENDADO', 'CONFIRMADO', 'EM_ATENDIMENTO', 'CONCLUIDO', 'CANCELADO'];

  readonly filtroForm = this.fb.group({
    status: this.fb.control<StatusAgendamento | ''>(''),
    tecnicoId: this.fb.control<number | ''>(''),
    dataInicial: this.fb.control<Date | null>(null),
    dataFinal: this.fb.control<Date | null>(null)
  });

  readonly agendamentos = signal<AgendamentoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly tecnicos = signal<TecnicoResumo[]>([]);

  readonly podeCriar = computed(() => this.authService.hasRole('ADMIN', 'ATENDENTE'));

  ngOnInit(): void {
    this.usuarioService.listarTecnicos().subscribe((tecnicos) => this.tecnicos.set(tecnicos));
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

    const { status, tecnicoId, dataInicial, dataFinal } = this.filtroForm.getRawValue();

    this.agendamentoService
      .listar(
        {
          status: status || undefined,
          tecnicoId: tecnicoId || undefined,
          dataInicial: dataInicial ? inicioDoDia(dataInicial) : undefined,
          dataFinal: dataFinal ? fimDoDia(dataFinal) : undefined
        },
        this.pageIndex(),
        this.pageSize()
      )
      .subscribe({
        next: (page) => {
          this.agendamentos.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar os agendamentos.'));
          this.loading.set(false);
        }
      });
  }
}
