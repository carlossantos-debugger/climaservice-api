import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { ClienteService } from '../../../core/services/cliente.service';
import { EquipamentoService } from '../../../core/services/equipamento.service';
import { ClienteResponse } from '../../../core/models/cliente.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

@Component({
  selector: 'app-equipamento-form',
  imports: [
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ErrorMessage,
    Loading
  ],
  templateUrl: './equipamento-form.html',
  styleUrl: './equipamento-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EquipamentoForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly equipamentoService = inject(EquipamentoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  private readonly equipamentoId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.equipamentoId() !== null);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly clientesEncontrados = signal<ClienteResponse[]>([]);

  private clienteSelecionadoId: number | null = null;
  /** Último texto que corresponde de fato a um cliente selecionado — usado para
   * distinguir "acabei de selecionar" de "o usuário editou o texto depois de selecionar". */
  private clienteSelecionadoLabel: string | null = null;

  readonly form = this.fb.group({
    marca: this.fb.control('', [Validators.required, Validators.maxLength(100)]),
    modelo: this.fb.control('', [Validators.required, Validators.maxLength(100)]),
    capacidadeBtu: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
    numeroSerie: this.fb.control('', [Validators.maxLength(100)]),
    localInstalacao: this.fb.control('', [Validators.maxLength(150)]),
    clienteBusca: this.fb.control('', [Validators.required])
  });

  ngOnInit(): void {
    this.form.controls.clienteBusca.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((nome) => {
          if (nome !== this.clienteSelecionadoLabel) {
            this.clienteSelecionadoId = null;
          }
          return nome && nome.length >= 2 ? this.clienteService.listar({ nome }, 0, 10) : of(null);
        })
      )
      .subscribe((page) => this.clientesEncontrados.set(page ? page.content : []));

    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      return;
    }

    const id = Number(idParam);
    this.equipamentoId.set(id);
    this.loading.set(true);

    this.equipamentoService.buscarPorId(id).subscribe({
      next: (equipamento) => {
        this.form.patchValue({
          marca: equipamento.marca,
          modelo: equipamento.modelo,
          capacidadeBtu: equipamento.capacidadeBtu,
          numeroSerie: equipamento.numeroSerie,
          localInstalacao: equipamento.localInstalacao,
          clienteBusca: equipamento.clienteNome
        });
        this.clienteSelecionadoId = equipamento.clienteId;
        this.clienteSelecionadoLabel = equipamento.clienteNome;
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar o equipamento.'));
        this.loading.set(false);
      }
    });
  }

  selecionarCliente(cliente: ClienteResponse): void {
    this.clienteSelecionadoId = cliente.id;
    this.clienteSelecionadoLabel = cliente.nome;
    this.form.controls.clienteBusca.setValue(cliente.nome);
    this.clientesEncontrados.set([]);
  }

  submit(): void {
    if (this.form.invalid || this.clienteSelecionadoId === null) {
      this.form.markAllAsTouched();
      if (this.clienteSelecionadoId === null) {
        this.errorMessage.set('Selecione um cliente da lista de sugestões.');
      }
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const { marca, modelo, capacidadeBtu, numeroSerie, localInstalacao } = this.form.getRawValue();
    const dto = {
      marca,
      modelo,
      capacidadeBtu: capacidadeBtu!,
      numeroSerie,
      localInstalacao,
      clienteId: this.clienteSelecionadoId
    };
    const id = this.equipamentoId();
    const request$ = id === null ? this.equipamentoService.salvar(dto) : this.equipamentoService.atualizar(id, dto);

    request$.subscribe({
      next: () => {
        this.snackBar.open(id === null ? 'Equipamento cadastrado.' : 'Equipamento atualizado.', 'Ok', {
          duration: 3000
        });
        this.router.navigateByUrl('/equipamentos');
      },
      error: (error: unknown) => {
        this.saving.set(false);
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível salvar o equipamento.'));
      }
    });
  }

  cancelar(): void {
    this.router.navigateByUrl('/equipamentos');
  }
}
