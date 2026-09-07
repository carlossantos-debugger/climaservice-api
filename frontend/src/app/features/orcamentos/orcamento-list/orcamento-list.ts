import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
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
import { OrcamentoService } from '../../../core/services/orcamento.service';
import { OrcamentoResponse, StatusOrcamento } from '../../../core/models/orcamento.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { fimDoDia, inicioDoDia } from '../../../core/utils/date-filtro.util';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

const STATUS_LABEL: Record<StatusOrcamento, string> = {
  RASCUNHO: 'Rascunho',
  ENVIADO: 'Enviado',
  APROVADO: 'Aprovado',
  REJEITADO: 'Rejeitado',
  CANCELADO: 'Cancelado'
};

@Component({
  selector: 'app-orcamento-list',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CurrencyPipe,
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
  templateUrl: './orcamento-list.html',
  styleUrl: './orcamento-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrcamentoList implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly orcamentoService = inject(OrcamentoService);

  readonly statusLabel = STATUS_LABEL;
  readonly statusOptions: StatusOrcamento[] = ['RASCUNHO', 'ENVIADO', 'APROVADO', 'REJEITADO', 'CANCELADO'];

  readonly filtroForm = this.fb.group({
    status: this.fb.control<StatusOrcamento | ''>(''),
    dataInicial: this.fb.control<Date | null>(null),
    dataFinal: this.fb.control<Date | null>(null)
  });

  readonly orcamentos = signal<OrcamentoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

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

    this.orcamentoService
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
          this.orcamentos.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar os orçamentos.'));
          this.loading.set(false);
        }
      });
  }
}
