import { ChangeDetectionStrategy, Component, inject, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-header',
  imports: [MatToolbarModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './header.html',
  styleUrl: './header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Header {
  private readonly snackBar = inject(MatSnackBar);

  /** Emitido ao clicar no botão de menu, visível apenas em telas menores (modo "over" do sidenav). */
  readonly menuToggle = output<void>();

  // TODO(feature/authentication): substituir pelos dados reais de AuthService
  // (nome/role do usuário logado, nome da empresa via GET /empresa/me).
  readonly empresaNome = 'ClimaService';
  readonly usuarioNome = '—';
  readonly usuarioRole = '—';

  logout(): void {
    this.snackBar.open('Login ainda não implementado (chega na feature/authentication).', 'Ok', {
      duration: 4000
    });
  }
}
