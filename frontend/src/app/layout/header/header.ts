import { ChangeDetectionStrategy, Component, inject, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-header',
  imports: [MatToolbarModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './header.html',
  styleUrl: './header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Header {
  private readonly authService = inject(AuthService);

  /** Emitido ao clicar no botão de menu, visível apenas em telas menores (modo "over" do sidenav). */
  readonly menuToggle = output<void>();

  readonly currentUser = this.authService.currentUser;

  // TODO(feature/empresa): nome real da empresa via GET /empresa/me.
  readonly empresaNome = 'ClimaService';

  logout(): void {
    this.authService.logout();
  }
}
