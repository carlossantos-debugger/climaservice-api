import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { OrdemServicoService } from '../../../core/services/ordem-servico.service';
import {
  OrdemServicoDiagnosticoHistorico,
  OrdemServicoHistorico,
  OrdemServicoResponse,
  StatusOrdemServico,
  TRANSICOES_STATUS_OS
} from '../../../core/models/ordem-servico.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
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
  selector: 'app-ordem-servico-detail',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    DatePipe,
    MatButtonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    ErrorMessage,
    Loading
  ],
  templateUrl: './ordem-servico-detail.html',
  styleUrl: './ordem-servico-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrdemServicoDetail implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly ordemServicoService = inject(OrdemServicoService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  readonly statusLabel = STATUS_LABEL;

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly os = signal<OrdemServicoResponse | null>(null);
  readonly historicoStatus = signal<OrdemServicoHistorico[]>([]);
  readonly historicoDiagnostico = signal<OrdemServicoDiagnosticoHistorico[]>([]);

  readonly salvandoDiagnostico = signal(false);
  readonly alterandoStatus = signal(false);

  readonly podeGerenciar = computed(() => this.authService.hasRole('ADMIN', 'TECNICO'));
  readonly diagnosticoEditavel = computed(() => {
    const os = this.os();
    return this.podeGerenciar() && !!os && os.status !== 'CONCLUIDA' && os.status !== 'CANCELADA';
  });
  readonly proximosStatus = computed(() => {
    const os = this.os();
    return os ? TRANSICOES_STATUS_OS[os.status] : [];
  });
  readonly podeAgendar = computed(() => {
    const os = this.os();
    return (
      this.authService.hasRole('ADMIN', 'ATENDENTE') && !!os && os.status !== 'CONCLUIDA' && os.status !== 'CANCELADA'
    );
  });

  readonly diagnosticoForm = this.fb.group({
    diagnostico: this.fb.control('', [Validators.maxLength(2000)])
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.carregar(id);
  }

  salvarDiagnostico(): void {
    const os = this.os();
    if (!os || this.diagnosticoForm.invalid) {
      return;
    }

    this.salvandoDiagnostico.set(true);

    this.ordemServicoService.atualizarDiagnostico(os.id, this.diagnosticoForm.getRawValue().diagnostico).subscribe({
      next: (atualizada) => {
        this.os.set(atualizada);
        this.salvandoDiagnostico.set(false);
        this.snackBar.open('Diagnóstico salvo.', 'Ok', { duration: 3000 });
        this.carregarHistoricoDiagnostico(os.id);
      },
      error: (error: unknown) => {
        this.salvandoDiagnostico.set(false);
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível salvar o diagnóstico.'), 'Ok', {
          duration: 4000
        });
      }
    });
  }

  alterarStatus(novoStatus: StatusOrdemServico): void {
    const os = this.os();
    if (!os) {
      return;
    }

    this.alterandoStatus.set(true);

    this.ordemServicoService.atualizarStatus(os.id, novoStatus).subscribe({
      next: (atualizada) => {
        this.os.set(atualizada);
        this.alterandoStatus.set(false);
        this.carregarHistoricoStatus(os.id);
      },
      error: (error: unknown) => {
        this.alterandoStatus.set(false);
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível alterar o status.'), 'Ok', {
          duration: 4000
        });
      }
    });
  }

  private carregar(id: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      os: this.ordemServicoService.buscarPorId(id),
      historicoStatus: this.ordemServicoService.listarHistorico(id),
      historicoDiagnostico: this.ordemServicoService.listarHistoricoDiagnostico(id)
    }).subscribe({
      next: ({ os, historicoStatus, historicoDiagnostico }) => {
        this.os.set(os);
        this.diagnosticoForm.controls.diagnostico.setValue(os.diagnostico ?? '');
        this.historicoStatus.set(historicoStatus);
        this.historicoDiagnostico.set(historicoDiagnostico);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar a ordem de serviço.'));
        this.loading.set(false);
      }
    });
  }

  private carregarHistoricoStatus(id: number): void {
    this.ordemServicoService.listarHistorico(id).subscribe((historico) => this.historicoStatus.set(historico));
  }

  private carregarHistoricoDiagnostico(id: number): void {
    this.ordemServicoService
      .listarHistoricoDiagnostico(id)
      .subscribe((historico) => this.historicoDiagnostico.set(historico));
  }
}
