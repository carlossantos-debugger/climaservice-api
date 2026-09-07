import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { OrcamentoService } from '../../../core/services/orcamento.service';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';

@Component({
  selector: 'app-orcamento-form',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, ErrorMessage],
  templateUrl: './orcamento-form.html',
  styleUrl: './orcamento-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrcamentoForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly orcamentoService = inject(OrcamentoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  private ordemServicoId: number | null = null;

  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    observacao: this.fb.control('', [Validators.maxLength(1000)])
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.queryParamMap.get('ordemServicoId');
    this.ordemServicoId = idParam ? Number(idParam) : null;

    if (this.ordemServicoId === null) {
      this.errorMessage.set('Nenhuma ordem de serviço informada — abra este formulário a partir de uma OS.');
    }
  }

  submit(): void {
    if (this.form.invalid || this.ordemServicoId === null) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    this.orcamentoService.criarParaOrdemServico(this.ordemServicoId, this.form.getRawValue()).subscribe({
      next: (orcamento) => {
        this.snackBar.open('Orçamento criado.', 'Ok', { duration: 3000 });
        this.router.navigate(['/orcamentos', orcamento.id]);
      },
      error: (error: unknown) => {
        this.saving.set(false);
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível criar o orçamento.'));
      }
    });
  }

  cancelar(): void {
    this.router.navigateByUrl('/orcamentos');
  }
}
