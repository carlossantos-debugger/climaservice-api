import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { EquipamentoService } from '../../../core/services/equipamento.service';
import { PlanoManutencaoService } from '../../../core/services/plano-manutencao.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { EquipamentoResponse } from '../../../core/models/equipamento.model';
import { Usuario } from '../../../core/models/usuario.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

@Component({
  selector: 'app-plano-form',
  imports: [
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ErrorMessage,
    Loading
  ],
  templateUrl: './plano-form.html',
  styleUrl: './plano-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlanoForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly equipamentoService = inject(EquipamentoService);
  private readonly planoService = inject(PlanoManutencaoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  private readonly planoId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.planoId() !== null);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly equipamentosEncontrados = signal<EquipamentoResponse[]>([]);
  readonly tecnicos = signal<Usuario[]>([]);
  readonly semPermissaoParaListarTecnicos = signal(false);

  private equipamentoSelecionadoId: number | null = null;
  private equipamentoSelecionadoLabel: string | null = null;
  /** Só mostrado em modo edição — o backend não permite trocar o equipamento do plano. */
  readonly equipamentoAtualLabel = signal<string | null>(null);

  readonly form = this.fb.group({
    equipamentoBusca: this.fb.control(''),
    tecnicoPadraoId: this.fb.control<number | null>(null),
    intervaloMeses: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
    proximaExecucao: this.fb.control(''),
    observacao: this.fb.control('', [Validators.maxLength(1000)])
  });

  ngOnInit(): void {
    this.usuarioService.listarTodos().subscribe({
      next: (usuarios) => this.tecnicos.set(usuarios.filter((u) => u.role === 'TECNICO' && u.ativo)),
      error: () => this.semPermissaoParaListarTecnicos.set(true)
    });

    this.form.controls.equipamentoBusca.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((marca) => {
          if (marca !== this.equipamentoSelecionadoLabel) {
            this.equipamentoSelecionadoId = null;
          }
          return marca && marca.length >= 2 ? this.equipamentoService.listar({ marca }, 0, 10) : of(null);
        })
      )
      .subscribe((page) => this.equipamentosEncontrados.set(page ? page.content : []));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.form.controls.equipamentoBusca.setValidators(Validators.required);
      return;
    }

    const id = Number(idParam);
    this.planoId.set(id);
    this.form.controls.proximaExecucao.addValidators(Validators.required);
    this.loading.set(true);

    this.planoService.buscarPorId(id).subscribe({
      next: (plano) => {
        this.equipamentoAtualLabel.set(`${plano.equipamentoMarca} ${plano.equipamentoModelo}`);
        this.equipamentoSelecionadoId = plano.equipamentoId;
        this.form.patchValue({
          tecnicoPadraoId: plano.tecnicoPadraoId ?? null,
          intervaloMeses: plano.intervaloMeses,
          proximaExecucao: plano.proximaExecucao ?? '',
          observacao: plano.observacao ?? ''
        });
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar o plano.'));
        this.loading.set(false);
      }
    });
  }

  selecionarEquipamento(equipamento: EquipamentoResponse): void {
    this.equipamentoSelecionadoId = equipamento.id;
    this.equipamentoSelecionadoLabel = `${equipamento.marca} ${equipamento.modelo}`;
    this.form.controls.equipamentoBusca.setValue(this.equipamentoSelecionadoLabel);
    this.equipamentosEncontrados.set([]);
  }

  submit(): void {
    if (this.form.invalid || this.equipamentoSelecionadoId === null) {
      this.form.markAllAsTouched();
      if (this.equipamentoSelecionadoId === null) {
        this.errorMessage.set('Selecione um equipamento da lista de sugestões.');
      }
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const { tecnicoPadraoId, intervaloMeses, proximaExecucao, observacao } = this.form.getRawValue();
    const id = this.planoId();

    const request$ =
      id === null
        ? this.planoService.criar({
            equipamentoId: this.equipamentoSelecionadoId,
            tecnicoPadraoId: tecnicoPadraoId ?? undefined,
            intervaloMeses: intervaloMeses!,
            proximaExecucao: proximaExecucao || undefined,
            observacao
          })
        : this.planoService.atualizar(id, {
            tecnicoPadraoId: tecnicoPadraoId ?? undefined,
            intervaloMeses: intervaloMeses!,
            proximaExecucao,
            observacao
          });

    request$.subscribe({
      next: () => {
        this.snackBar.open(id === null ? 'Plano criado.' : 'Plano atualizado.', 'Ok', { duration: 3000 });
        this.router.navigateByUrl('/manutencoes-preventivas');
      },
      error: (error: unknown) => {
        this.saving.set(false);
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível salvar o plano.'));
      }
    });
  }

  cancelar(): void {
    this.router.navigateByUrl('/manutencoes-preventivas');
  }
}
