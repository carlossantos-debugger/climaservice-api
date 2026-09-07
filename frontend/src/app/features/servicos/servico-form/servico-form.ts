import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { ServicoService } from '../../../core/services/servico.service';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

@Component({
  selector: 'app-servico-form',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, ErrorMessage, Loading],
  templateUrl: './servico-form.html',
  styleUrl: './servico-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServicoForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly servicoService = inject(ServicoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  private readonly servicoId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.servicoId() !== null);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    nome: this.fb.control('', [Validators.required, Validators.maxLength(150)]),
    descricao: this.fb.control('', [Validators.maxLength(500)]),
    valorPadrao: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)])
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      return;
    }

    const id = Number(idParam);
    this.servicoId.set(id);
    this.loading.set(true);

    this.servicoService.buscarPorId(id).subscribe({
      next: (servico) => {
        this.form.patchValue(servico);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar o serviço.'));
        this.loading.set(false);
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const { nome, descricao, valorPadrao } = this.form.getRawValue();
    const dto = { nome, descricao, valorPadrao: valorPadrao! };
    const id = this.servicoId();
    const request$ = id === null ? this.servicoService.salvar(dto) : this.servicoService.atualizar(id, dto);

    request$.subscribe({
      next: () => {
        this.snackBar.open(id === null ? 'Serviço cadastrado.' : 'Serviço atualizado.', 'Ok', { duration: 3000 });
        this.router.navigateByUrl('/servicos');
      },
      error: (error: unknown) => {
        this.saving.set(false);
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível salvar o serviço.'));
      }
    });
  }

  cancelar(): void {
    this.router.navigateByUrl('/servicos');
  }
}
