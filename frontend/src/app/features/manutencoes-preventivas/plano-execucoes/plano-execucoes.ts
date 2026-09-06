import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { PlanoManutencaoService } from '../../../core/services/plano-manutencao.service';
import { PlanoManutencaoExecucao, PlanoManutencaoResponse } from '../../../core/models/plano-manutencao.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

@Component({
  selector: 'app-plano-execucoes',
  imports: [RouterLink, DatePipe, MatListModule, EmptyState, ErrorMessage, Loading],
  templateUrl: './plano-execucoes.html',
  styleUrl: './plano-execucoes.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlanoExecucoes implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly planoService = inject(PlanoManutencaoService);

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly plano = signal<PlanoManutencaoResponse | null>(null);
  readonly execucoes = signal<PlanoManutencaoExecucao[]>([]);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    forkJoin({
      plano: this.planoService.buscarPorId(id),
      execucoes: this.planoService.listarExecucoes(id)
    }).subscribe({
      next: ({ plano, execucoes }) => {
        this.plano.set(plano);
        this.execucoes.set(execucoes);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar as execuções do plano.'));
        this.loading.set(false);
      }
    });
  }
}
