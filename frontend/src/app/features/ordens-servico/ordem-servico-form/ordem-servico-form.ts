import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { ClienteService } from '../../../core/services/cliente.service';
import { EquipamentoService } from '../../../core/services/equipamento.service';
import { OrdemServicoService } from '../../../core/services/ordem-servico.service';
import { ClienteResponse } from '../../../core/models/cliente.model';
import { EquipamentoResponse } from '../../../core/models/equipamento.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';

@Component({
  selector: 'app-ordem-servico-form',
  imports: [
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ErrorMessage
  ],
  templateUrl: './ordem-servico-form.html',
  styleUrl: './ordem-servico-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrdemServicoForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly equipamentoService = inject(EquipamentoService);
  private readonly ordemServicoService = inject(OrdemServicoService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly clientesEncontrados = signal<ClienteResponse[]>([]);
  readonly equipamentosDoCliente = signal<EquipamentoResponse[]>([]);
  readonly carregandoEquipamentos = signal(false);

  private clienteSelecionadoId: number | null = null;
  private clienteSelecionadoLabel: string | null = null;

  readonly form = this.fb.group({
    clienteBusca: this.fb.control('', [Validators.required]),
    equipamentoId: this.fb.control<number | null>(null, [Validators.required]),
    descricaoProblema: this.fb.control('', [Validators.required, Validators.maxLength(1000)])
  });

  ngOnInit(): void {
    this.form.controls.clienteBusca.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((nome) => {
          if (nome !== this.clienteSelecionadoLabel) {
            this.clienteSelecionadoId = null;
            this.equipamentosDoCliente.set([]);
            this.form.controls.equipamentoId.setValue(null);
          }
          return nome && nome.length >= 2 ? this.clienteService.listar({ nome }, 0, 10) : of(null);
        })
      )
      .subscribe((page) => this.clientesEncontrados.set(page ? page.content : []));
  }

  selecionarCliente(cliente: ClienteResponse): void {
    this.clienteSelecionadoId = cliente.id;
    this.clienteSelecionadoLabel = cliente.nome;
    this.form.controls.clienteBusca.setValue(cliente.nome);
    this.clientesEncontrados.set([]);

    this.carregandoEquipamentos.set(true);
    this.equipamentoService.listarAtivosPorCliente(cliente.id).subscribe({
      next: (equipamentos) => {
        this.equipamentosDoCliente.set(equipamentos);
        this.carregandoEquipamentos.set(false);
      },
      error: () => {
        this.equipamentosDoCliente.set([]);
        this.carregandoEquipamentos.set(false);
      }
    });
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

    const { equipamentoId, descricaoProblema } = this.form.getRawValue();

    this.ordemServicoService
      .salvar({ clienteId: this.clienteSelecionadoId, equipamentoId: equipamentoId!, descricaoProblema })
      .subscribe({
        next: (os) => {
          this.snackBar.open('Ordem de serviço aberta.', 'Ok', { duration: 3000 });
          this.router.navigate(['/ordens-servico', os.id]);
        },
        error: (error: unknown) => {
          this.saving.set(false);
          this.errorMessage.set(extractErrorMessage(error, 'Não foi possível abrir a ordem de serviço.'));
        }
      });
  }

  cancelar(): void {
    this.router.navigateByUrl('/ordens-servico');
  }
}
