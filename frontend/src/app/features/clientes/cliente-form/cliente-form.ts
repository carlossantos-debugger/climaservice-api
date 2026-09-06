import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { ClienteService } from '../../../core/services/cliente.service';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

const CPF_CNPJ_PATTERN = /^\d{11}(\d{3})?$/;

@Component({
  selector: 'app-cliente-form',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, ErrorMessage, Loading],
  templateUrl: './cliente-form.html',
  styleUrl: './cliente-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ClienteForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  private readonly clienteId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.clienteId() !== null);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    nome: this.fb.control('', [Validators.required, Validators.maxLength(150)]),
    cpfCnpj: this.fb.control('', [Validators.required, Validators.pattern(CPF_CNPJ_PATTERN)]),
    telefone: this.fb.control('', [Validators.maxLength(20)]),
    email: this.fb.control('', [Validators.email, Validators.maxLength(150)]),
    inscricaoMunicipal: this.fb.control('', [Validators.maxLength(30)]),
    inscricaoEstadual: this.fb.control('', [Validators.maxLength(30)]),
    endereco: this.fb.group({
      logradouro: this.fb.control('', [Validators.maxLength(150)]),
      numero: this.fb.control('', [Validators.maxLength(20)]),
      complemento: this.fb.control('', [Validators.maxLength(100)]),
      bairro: this.fb.control('', [Validators.maxLength(100)]),
      cidade: this.fb.control('', [Validators.maxLength(100)]),
      uf: this.fb.control('', [Validators.maxLength(2)]),
      cep: this.fb.control('', [Validators.maxLength(10)])
    })
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      return;
    }

    const id = Number(idParam);
    this.clienteId.set(id);
    this.loading.set(true);

    this.clienteService.buscarPorId(id).subscribe({
      next: (cliente) => {
        // endereco pode vir null do backend (campo opcional) — FormGroup.patchValue(null)
        // numa nested FormGroup lança erro, por isso o fallback para {}.
        this.form.patchValue({ ...cliente, endereco: cliente.endereco ?? {} });
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar o cliente.'));
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

    const dto = this.form.getRawValue();
    const id = this.clienteId();
    const request$ = id === null ? this.clienteService.salvar(dto) : this.clienteService.atualizar(id, dto);

    request$.subscribe({
      next: () => {
        this.snackBar.open(id === null ? 'Cliente cadastrado.' : 'Cliente atualizado.', 'Ok', { duration: 3000 });
        this.router.navigateByUrl('/clientes');
      },
      error: (error: unknown) => {
        this.saving.set(false);
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível salvar o cliente.'));
      }
    });
  }

  cancelar(): void {
    this.router.navigateByUrl('/clientes');
  }
}
