import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ProdutoService } from '../../../core/services/produto.service';
import { ProdutoResponse } from '../../../core/models/produto.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

@Component({
  selector: 'app-produto-list',
  imports: [RouterLink, CurrencyPipe, MatButtonModule, MatChipsModule, MatIconModule, EmptyState, ErrorMessage, Loading],
  templateUrl: './produto-list.html',
  styleUrl: './produto-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProdutoList implements OnInit {
  private readonly produtoService = inject(ProdutoService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  readonly produtos = signal<ProdutoResponse[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly podeGerenciar = computed(() => this.authService.hasRole('ADMIN', 'ATENDENTE'));

  ngOnInit(): void {
    this.carregar();
  }

  alternarAtivo(produto: ProdutoResponse): void {
    const request$ = produto.ativo ? this.produtoService.inativar(produto.id) : this.produtoService.ativar(produto.id);

    request$.subscribe({
      next: () => this.carregar(),
      error: (error: unknown) => {
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível alterar o status do produto.'), 'Ok', {
          duration: 4000
        });
      }
    });
  }

  private carregar(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.produtoService.listarTodos().subscribe({
      next: (produtos) => {
        this.produtos.set(produtos);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar os produtos.'));
        this.loading.set(false);
      }
    });
  }
}
