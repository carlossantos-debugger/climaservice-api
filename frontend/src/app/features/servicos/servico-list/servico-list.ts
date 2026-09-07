import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ServicoService } from '../../../core/services/servico.service';
import { ServicoResponse } from '../../../core/models/servico.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

@Component({
  selector: 'app-servico-list',
  imports: [RouterLink, CurrencyPipe, MatButtonModule, MatChipsModule, MatIconModule, EmptyState, ErrorMessage, Loading],
  templateUrl: './servico-list.html',
  styleUrl: './servico-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServicoList implements OnInit {
  private readonly servicoService = inject(ServicoService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  readonly servicos = signal<ServicoResponse[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly podeGerenciar = computed(() => this.authService.hasRole('ADMIN', 'ATENDENTE'));

  ngOnInit(): void {
    this.carregar();
  }

  alternarAtivo(servico: ServicoResponse): void {
    const request$ = servico.ativo ? this.servicoService.inativar(servico.id) : this.servicoService.ativar(servico.id);

    request$.subscribe({
      next: () => this.carregar(),
      error: (error: unknown) => {
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível alterar o status do serviço.'), 'Ok', {
          duration: 4000
        });
      }
    });
  }

  private carregar(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.servicoService.listarTodos().subscribe({
      next: (servicos) => {
        this.servicos.set(servicos);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar os serviços.'));
        this.loading.set(false);
      }
    });
  }
}
