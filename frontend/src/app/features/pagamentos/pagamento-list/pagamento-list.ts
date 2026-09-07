import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
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
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { PagamentoService } from '../../../core/services/pagamento.service';
import { FormaPagamento, PagamentoResponse, StatusPagamento } from '../../../core/models/pagamento.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { fimDoDia, inicioDoDia } from '../../../core/utils/date-filtro.util';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

const STATUS_LABEL: Record<StatusPagamento, string> = {
  PENDENTE: 'Pendente',
  CONFIRMADO: 'Confirmado',
  CANCELADO: 'Cancelado'
};

const FORMA_LABEL: Record<FormaPagamento, string> = {
  DINHEIRO: 'Dinheiro',
  PIX: 'Pix',
  CARTAO_CREDITO: 'Cartão de crédito',
  CARTAO_DEBITO: 'Cartão de débito',
  BOLETO: 'Boleto',
  TRANSFERENCIA: 'Transferência'
};

@Component({
  selector: 'app-pagamento-list',
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
  templateUrl: './pagamento-list.html',
  styleUrl: './pagamento-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PagamentoList implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly pagamentoService = inject(PagamentoService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  readonly podeGerenciar = computed(() => this.authService.hasRole('ADMIN', 'ATENDENTE'));

  readonly statusLabel = STATUS_LABEL;
  readonly formaLabel = FORMA_LABEL;
  readonly statusOptions: StatusPagamento[] = ['PENDENTE', 'CONFIRMADO', 'CANCELADO'];
  readonly formaOptions: FormaPagamento[] = [
    'DINHEIRO',
    'PIX',
    'CARTAO_CREDITO',
    'CARTAO_DEBITO',
    'BOLETO',
    'TRANSFERENCIA'
  ];

  readonly filtroForm = this.fb.group({
    status: this.fb.control<StatusPagamento | ''>(''),
    formaPagamento: this.fb.control<FormaPagamento | ''>(''),
    dataInicial: this.fb.control<Date | null>(null),
    dataFinal: this.fb.control<Date | null>(null)
  });

  readonly pagamentos = signal<PagamentoResponse[]>([]);
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

  confirmar(pagamento: PagamentoResponse): void {
    this.pagamentoService.confirmar(pagamento.id).subscribe({
      next: () => this.carregar(),
      error: (error: unknown) => {
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível confirmar o pagamento.'), 'Ok', {
          duration: 4000
        });
      }
    });
  }

  cancelar(pagamento: PagamentoResponse): void {
    this.pagamentoService.cancelar(pagamento.id).subscribe({
      next: () => this.carregar(),
      error: (error: unknown) => {
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível cancelar o pagamento.'), 'Ok', {
          duration: 4000
        });
      }
    });
  }

  private carregar(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const { status, formaPagamento, dataInicial, dataFinal } = this.filtroForm.getRawValue();

    this.pagamentoService
      .listar(
        {
          status: status || undefined,
          formaPagamento: formaPagamento || undefined,
          dataInicial: dataInicial ? inicioDoDia(dataInicial) : undefined,
          dataFinal: dataFinal ? fimDoDia(dataFinal) : undefined
        },
        this.pageIndex(),
        this.pageSize()
      )
      .subscribe({
        next: (page) => {
          this.pagamentos.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar os pagamentos.'));
          this.loading.set(false);
        }
      });
  }
}
