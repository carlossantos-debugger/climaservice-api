import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { EquipamentoService } from '../../../core/services/equipamento.service';
import { EquipamentoResponse, StatusEquipamento } from '../../../core/models/equipamento.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

@Component({
  selector: 'app-equipamento-list',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
    MatTooltipModule,
    EmptyState,
    ErrorMessage,
    Loading
  ],
  templateUrl: './equipamento-list.html',
  styleUrl: './equipamento-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EquipamentoList implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly equipamentoService = inject(EquipamentoService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly authService = inject(AuthService);

  readonly displayedColumns = ['marca', 'modelo', 'capacidadeBtu', 'cliente', 'status', 'acoes'];

  readonly filtroForm = this.fb.group({
    marca: this.fb.control(''),
    modelo: this.fb.control(''),
    status: this.fb.control<StatusEquipamento | ''>('')
  });

  readonly equipamentos = signal<EquipamentoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly podeEditar = computed(() => this.authService.hasRole('ADMIN', 'ATENDENTE'));

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

  alternarStatus(equipamento: EquipamentoResponse): void {
    const request$ =
      equipamento.status === 'ATIVO'
        ? this.equipamentoService.inativar(equipamento.id)
        : this.equipamentoService.ativar(equipamento.id);

    request$.subscribe({
      next: () => this.carregar(),
      error: (error: unknown) => {
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível alterar o status.'), 'Ok', {
          duration: 4000
        });
      }
    });
  }

  private carregar(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const { marca, modelo, status } = this.filtroForm.getRawValue();

    this.equipamentoService
      .listar({ marca, modelo, status: status || undefined }, this.pageIndex(), this.pageSize())
      .subscribe({
        next: (page) => {
          this.equipamentos.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar os equipamentos.'));
          this.loading.set(false);
        }
      });
  }
}
