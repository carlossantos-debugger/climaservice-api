import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { UsuarioService } from '../../../core/services/usuario.service';
import { Role } from '../../../core/models/role.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';

@Component({
  selector: 'app-usuario-form',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, ErrorMessage],
  templateUrl: './usuario-form.html',
  styleUrl: './usuario-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UsuarioForm {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly roles: Role[] = ['ADMIN', 'ATENDENTE', 'TECNICO'];

  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    nome: this.fb.control('', [Validators.required, Validators.maxLength(150)]),
    email: this.fb.control('', [Validators.required, Validators.email, Validators.maxLength(150)]),
    senha: this.fb.control('', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]),
    role: this.fb.control<Role | null>(null, [Validators.required])
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const { nome, email, senha, role } = this.form.getRawValue();

    this.usuarioService.cadastrar({ nome, email, senha, role: role! }).subscribe({
      next: () => {
        this.snackBar.open('Usuário cadastrado.', 'Ok', { duration: 3000 });
        this.router.navigateByUrl('/usuarios');
      },
      error: (error: unknown) => {
        this.saving.set(false);
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível cadastrar o usuário.'));
      }
    });
  }

  cancelar(): void {
    this.router.navigateByUrl('/usuarios');
  }
}
