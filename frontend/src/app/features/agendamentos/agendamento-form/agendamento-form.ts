import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { AgendamentoService } from '../../../core/services/agendamento.service';
import { OrdemServicoService } from '../../../core/services/ordem-servico.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { OrdemServicoResponse } from '../../../core/models/ordem-servico.model';
import { Usuario } from '../../../core/models/usuario.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';

@Component({
  selector: 'app-agendamento-form',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    ErrorMessage
  ],
  templateUrl: './agendamento-form.html',
  styleUrl: './agendamento-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgendamentoForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly agendamentoService = inject(AgendamentoService);
  private readonly ordemServicoService = inject(OrdemServicoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly snackBar = inject(MatSnackBar);

  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly buscandoOs = signal(false);
  readonly ordemServico = signal<OrdemServicoResponse | null>(null);
  readonly osNaoEncontrada = signal(false);

  readonly tecnicos = signal<Usuario[]>([]);
  /** true quando GET /usuarios deu 403 (usuário ATENDENTE) — ver UsuarioService. */
  readonly semPermissaoParaListarTecnicos = signal(false);

  readonly form = this.fb.group({
    ordemServicoId: this.fb.control<number | null>(null, [Validators.required]),
    tecnicoId: this.fb.control<number | null>(null, [Validators.required]),
    dataHoraInicio: this.fb.control('', [Validators.required]),
    dataHoraFim: this.fb.control('', [Validators.required]),
    observacao: this.fb.control('', [Validators.maxLength(1000)])
  });

  ngOnInit(): void {
    this.usuarioService.listarTodos().subscribe({
      next: (usuarios) => this.tecnicos.set(usuarios.filter((u) => u.role === 'TECNICO' && u.ativo)),
      error: () => this.semPermissaoParaListarTecnicos.set(true)
    });

    const ordemServicoIdParam = this.route.snapshot.queryParamMap.get('ordemServicoId');
    if (ordemServicoIdParam) {
      const id = Number(ordemServicoIdParam);
      this.form.controls.ordemServicoId.setValue(id);
      this.buscarOs(id);
    }
  }

  buscarOs(idParam?: number): void {
    const id = idParam ?? this.form.controls.ordemServicoId.value;
    if (!id) {
      return;
    }

    this.buscandoOs.set(true);
    this.osNaoEncontrada.set(false);
    this.ordemServico.set(null);

    this.ordemServicoService.buscarPorId(id).subscribe({
      next: (os) => {
        this.ordemServico.set(os);
        this.buscandoOs.set(false);
      },
      error: () => {
        this.osNaoEncontrada.set(true);
        this.buscandoOs.set(false);
      }
    });
  }

  submit(): void {
    const os = this.ordemServico();

    if (this.form.invalid || !os) {
      this.form.markAllAsTouched();
      if (!os) {
        this.errorMessage.set('Busque e confirme a ordem de serviço antes de salvar.');
      }
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const { tecnicoId, dataHoraInicio, dataHoraFim, observacao } = this.form.getRawValue();

    this.agendamentoService
      .criar({
        ordemServicoId: os.id,
        tecnicoId: tecnicoId!,
        dataHoraInicio,
        dataHoraFim,
        observacao
      })
      .subscribe({
        next: (agendamento) => {
          this.snackBar.open('Agendamento criado.', 'Ok', { duration: 3000 });
          this.router.navigate(['/agendamentos', agendamento.id]);
        },
        error: (error: unknown) => {
          this.saving.set(false);
          this.errorMessage.set(extractErrorMessage(error, 'Não foi possível criar o agendamento.'));
        }
      });
  }

  cancelar(): void {
    this.router.navigateByUrl('/agendamentos');
  }
}
