import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';
import { EmpresaService } from '../../../core/services/empresa.service';
import { RegimeTributario } from '../../../core/models/empresa.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

const REGIME_LABEL: Record<RegimeTributario, string> = {
  SIMPLES_NACIONAL: 'Simples Nacional',
  LUCRO_PRESUMIDO: 'Lucro Presumido',
  LUCRO_REAL: 'Lucro Real',
  MEI: 'MEI'
};

@Component({
  selector: 'app-empresa-page',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatButtonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ErrorMessage,
    Loading
  ],
  templateUrl: './empresa-page.html',
  styleUrl: './empresa-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EmpresaPage implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly empresaService = inject(EmpresaService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  readonly regimeLabel = REGIME_LABEL;
  readonly regimes: RegimeTributario[] = ['SIMPLES_NACIONAL', 'LUCRO_PRESUMIDO', 'LUCRO_REAL', 'MEI'];

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly ativo = signal(false);
  readonly dataCriacao = signal<string | null>(null);

  readonly podeEditar = computed(() => this.authService.hasRole('ADMIN'));

  readonly form = this.fb.group({
    nome: this.fb.control('', [Validators.required, Validators.maxLength(150)]),
    cpfCnpj: this.fb.control('', [Validators.pattern(/^\d{11}(\d{3})?$/)]),
    inscricaoMunicipal: this.fb.control('', [Validators.maxLength(30)]),
    regimeTributario: this.fb.control<RegimeTributario | null>(null),
    codigoServicoPadrao: this.fb.control('', [Validators.maxLength(20)]),
    aliquotaIssPadrao: this.fb.control<number | null>(null, [Validators.min(0), Validators.max(100)]),
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
    this.empresaService.obterAtual().subscribe({
      next: (empresa) => {
        this.ativo.set(empresa.ativo);
        this.dataCriacao.set(empresa.dataCriacao);
        this.form.patchValue({ ...empresa, endereco: empresa.endereco ?? {} });
        if (!this.podeEditar()) {
          this.form.disable();
        }
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar os dados da empresa.'));
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

    const { regimeTributario, aliquotaIssPadrao, ...resto } = this.form.getRawValue();

    this.empresaService
      .atualizar({
        ...resto,
        regimeTributario: regimeTributario ?? undefined,
        aliquotaIssPadrao: aliquotaIssPadrao ?? undefined
      })
      .subscribe({
        next: (empresa) => {
          this.saving.set(false);
          this.form.patchValue({ ...empresa, endereco: empresa.endereco ?? {} });
          this.snackBar.open('Dados da empresa atualizados.', 'Ok', { duration: 3000 });
        },
        error: (error: unknown) => {
          this.saving.set(false);
          this.errorMessage.set(extractErrorMessage(error, 'Não foi possível salvar os dados da empresa.'));
        }
      });
  }
}
