import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AgendamentoService } from '../../../core/services/agendamento.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  AgendamentoHistorico,
  AgendamentoResponse,
  StatusAgendamento,
  TRANSICOES_STATUS_AGENDAMENTO
} from '../../../core/models/agendamento.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
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
  selector: 'app-agendamento-detail',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    DatePipe,
    MatButtonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
    ErrorMessage,
    Loading
  ],
  templateUrl: './agendamento-detail.html',
  styleUrl: './agendamento-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgendamentoDetail implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly agendamentoService = inject(AgendamentoService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  readonly statusLabel = STATUS_LABEL;

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly agendamento = signal<AgendamentoResponse | null>(null);
  readonly historico = signal<AgendamentoHistorico[]>([]);

  readonly alterandoStatus = signal(false);
  readonly reagendando = signal(false);

  readonly podeAlterarStatus = computed(() => this.authService.hasRole('ADMIN', 'TECNICO'));
  readonly podeReagendar = computed(() => {
    const agendamento = this.agendamento();
    return (
      this.authService.hasRole('ADMIN', 'ATENDENTE') &&
      !!agendamento &&
      agendamento.status !== 'CONCLUIDO' &&
      agendamento.status !== 'CANCELADO'
    );
  });
  readonly proximosStatus = computed(() => {
    const agendamento = this.agendamento();
    return agendamento ? TRANSICOES_STATUS_AGENDAMENTO[agendamento.status] : [];
  });

  readonly reagendarForm = this.fb.group({
    dataHoraInicio: this.fb.control('', [Validators.required]),
    dataHoraFim: this.fb.control('', [Validators.required])
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.carregar(id);
  }

  alterarStatus(novoStatus: StatusAgendamento): void {
    const agendamento = this.agendamento();
    if (!agendamento) {
      return;
    }

    this.alterandoStatus.set(true);

    this.agendamentoService.atualizarStatus(agendamento.id, novoStatus).subscribe({
      next: (atualizado) => {
        this.agendamento.set(atualizado);
        this.alterandoStatus.set(false);
        this.carregarHistorico(agendamento.id);
      },
      error: (error: unknown) => {
        this.alterandoStatus.set(false);
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível alterar o status.'), 'Ok', {
          duration: 4000
        });
      }
    });
  }

  reagendar(): void {
    const agendamento = this.agendamento();
    if (!agendamento || this.reagendarForm.invalid) {
      this.reagendarForm.markAllAsTouched();
      return;
    }

    this.reagendando.set(true);

    this.agendamentoService.reagendar(agendamento.id, this.reagendarForm.getRawValue()).subscribe({
      next: (atualizado) => {
        this.agendamento.set(atualizado);
        this.reagendando.set(false);
        this.reagendarForm.reset();
        this.snackBar.open('Agendamento reagendado.', 'Ok', { duration: 3000 });
      },
      error: (error: unknown) => {
        this.reagendando.set(false);
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível reagendar.'), 'Ok', { duration: 4000 });
      }
    });
  }

  private carregar(id: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      agendamento: this.agendamentoService.buscarPorId(id),
      historico: this.agendamentoService.listarHistorico(id)
    }).subscribe({
      next: ({ agendamento, historico }) => {
        this.agendamento.set(agendamento);
        this.historico.set(historico);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar o agendamento.'));
        this.loading.set(false);
      }
    });
  }

  private carregarHistorico(id: number): void {
    this.agendamentoService.listarHistorico(id).subscribe((historico) => this.historico.set(historico));
  }
}
