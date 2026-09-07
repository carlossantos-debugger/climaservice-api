import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { UsuarioService } from '../../../core/services/usuario.service';
import { Usuario } from '../../../core/models/usuario.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

@Component({
  selector: 'app-usuario-list',
  imports: [RouterLink, MatButtonModule, MatChipsModule, MatIconModule, MatTooltipModule, EmptyState, ErrorMessage, Loading],
  templateUrl: './usuario-list.html',
  styleUrl: './usuario-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UsuarioList implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly snackBar = inject(MatSnackBar);

  readonly usuarios = signal<Usuario[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.carregar();
  }

  /**
   * Bloqueia visualmente o toggle no único ADMIN ativo — o backend já impede isso
   * (README: "Nunca ficar sem ADMIN ativo"), isso aqui só evita um clique que sempre falharia.
   */
  ehUnicoAdminAtivo(usuario: Usuario): boolean {
    if (usuario.role !== 'ADMIN' || !usuario.ativo) {
      return false;
    }
    return this.usuarios().filter((u) => u.role === 'ADMIN' && u.ativo).length === 1;
  }

  alternarAtivo(usuario: Usuario): void {
    const request$ = usuario.ativo ? this.usuarioService.inativar(usuario.id) : this.usuarioService.ativar(usuario.id);

    request$.subscribe({
      next: () => this.carregar(),
      error: (error: unknown) => {
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível alterar o status do usuário.'), 'Ok', {
          duration: 4000
        });
      }
    });
  }

  private carregar(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.usuarioService.listarTodos().subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar os usuários.'));
        this.loading.set(false);
      }
    });
  }
}
