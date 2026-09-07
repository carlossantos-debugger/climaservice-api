import { ChangeDetectionStrategy, Component, OnInit, inject, output, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/services/auth.service';
import { EmpresaService } from '../../core/services/empresa.service';

@Component({
  selector: 'app-header',
  imports: [MatToolbarModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './header.html',
  styleUrl: './header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Header implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly empresaService = inject(EmpresaService);

  /** Emitido ao clicar no botão de menu, visível apenas em telas menores (modo "over" do sidenav). */
  readonly menuToggle = output<void>();

  readonly currentUser = this.authService.currentUser;

  /** "ClimaService" até GET /empresa/me responder — nome genérico, não o de nenhuma empresa real. */
  readonly empresaNome = signal('ClimaService');

  ngOnInit(): void {
    // Sem tratamento de erro específico aqui: em falha, só mantém o nome genérico —
    // não vale a pena um snackbar só por isso no cabeçalho.
    this.empresaService.obterAtual().subscribe({
      next: (empresa) => this.empresaNome.set(empresa.nome),
      error: () => undefined
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
