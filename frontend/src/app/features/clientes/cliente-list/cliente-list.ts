import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AuthService } from '../../../core/services/auth.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { ClienteResponse } from '../../../core/models/cliente.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ConfirmDialogService } from '../../../shared/components/confirm-dialog/confirm-dialog.service';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-cliente-list',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatTableModule,
    EmptyState,
    ErrorMessage,
    Loading
  ],
  templateUrl: './cliente-list.html',
  styleUrl: './cliente-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ClienteList implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly authService = inject(AuthService);

  readonly displayedColumns = ['nome', 'cpfCnpj', 'telefone', 'email', 'acoes'];

  readonly filtroForm = this.fb.group({
    nome: this.fb.control(''),
    cpfCnpj: this.fb.control('')
  });

  readonly clientes = signal<ClienteResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly podeEditar = computed(() => this.authService.hasRole('ADMIN', 'ATENDENTE'));
  readonly podeExcluir = computed(() => this.authService.hasRole('ADMIN'));

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

  excluir(cliente: ClienteResponse): void {
    this.confirmDialog
      .confirm({
        title: 'Excluir cliente',
        message: `Tem certeza que deseja excluir "${cliente.nome}"? Essa ação não pode ser desfeita.`,
        confirmText: 'Excluir',
        destructive: true
      })
      .subscribe((confirmado) => {
        if (!confirmado) {
          return;
        }

        this.clienteService.excluir(cliente.id).subscribe({
          next: () => {
            this.snackBar.open('Cliente excluído.', 'Ok', { duration: 3000 });
            this.carregar();
          },
          error: (error: unknown) => {
            this.snackBar.open(extractErrorMessage(error, 'Não foi possível excluir o cliente.'), 'Ok', {
              duration: 4000
            });
          }
        });
      });
  }

  private carregar(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.clienteService.listar(this.filtroForm.getRawValue(), this.pageIndex(), this.pageSize()).subscribe({
      next: (page) => {
        this.clientes.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar os clientes.'));
        this.loading.set(false);
      }
    });
  }
}
