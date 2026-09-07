import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { PlanoManutencaoService } from '../../../core/services/plano-manutencao.service';
import { PlanoManutencaoResponse } from '../../../core/models/plano-manutencao.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ConfirmDialogService } from '../../../shared/components/confirm-dialog/confirm-dialog.service';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

@Component({
  selector: 'app-plano-list',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    DatePipe,
    MatButtonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatSelectModule,
    MatTooltipModule,
    EmptyState,
    ErrorMessage,
    Loading
  ],
  templateUrl: './plano-list.html',
  styleUrl: './plano-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlanoList implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly planoService = inject(PlanoManutencaoService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly authService = inject(AuthService);

  readonly filtroForm = this.fb.group({
    ativo: this.fb.control<'true' | 'false' | ''>('true')
  });

  readonly planos = signal<PlanoManutencaoResponse[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly podeGerenciar = computed(() => this.authService.hasRole('ADMIN', 'ATENDENTE'));

  ngOnInit(): void {
    this.filtroForm.controls.ativo.valueChanges.subscribe(() => this.carregar());
    this.carregar();
  }

  alternarAtivo(plano: PlanoManutencaoResponse): void {
    const request$ = plano.ativo ? this.planoService.inativar(plano.id) : this.planoService.ativar(plano.id);

    request$.subscribe({
      next: () => this.carregar(),
      error: (error: unknown) => {
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível alterar o status do plano.'), 'Ok', {
          duration: 4000
        });
      }
    });
  }

  gerarOrdemServico(plano: PlanoManutencaoResponse): void {
    this.confirmDialog
      .confirm({
        title: 'Gerar ordem de serviço',
        message: `Gerar uma OS preventiva para ${plano.equipamentoMarca} ${plano.equipamentoModelo} agora?`,
        confirmText: 'Gerar OS'
      })
      .subscribe((confirmado) => {
        if (!confirmado) {
          return;
        }

        this.planoService.gerarOrdemServico(plano.id).subscribe({
          next: (execucao) => {
            this.snackBar.open(`OS #${execucao.ordemServicoId} gerada.`, 'Ok', { duration: 4000 });
            this.carregar();
          },
          error: (error: unknown) => {
            this.snackBar.open(extractErrorMessage(error, 'Não foi possível gerar a ordem de serviço.'), 'Ok', {
              duration: 4000
            });
          }
        });
      });
  }

  private carregar(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const ativoRaw = this.filtroForm.controls.ativo.value;
    const ativo = ativoRaw === '' ? undefined : ativoRaw === 'true';

    this.planoService.listar({ ativo }).subscribe({
      next: (planos) => {
        this.planos.set(planos);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar os planos de manutenção.'));
        this.loading.set(false);
      }
    });
  }
}
