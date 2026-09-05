import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

/**
 * Página provisória usada pelas rotas ainda não implementadas (branches 4 a 12
 * substituem cada rota por sua própria feature). Título e ícone vêm de `route.data`
 * em app.routes.ts — não é preciso criar um componente por módulo só para isso.
 */
@Component({
  selector: 'app-placeholder-page',
  imports: [MatIconModule],
  templateUrl: './placeholder-page.html',
  styleUrl: './placeholder-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlaceholderPage {
  private readonly route = inject(ActivatedRoute);

  readonly title = toSignal(this.route.data.pipe(map((data) => data['title'] as string)), {
    initialValue: ''
  });
  readonly icon = toSignal(this.route.data.pipe(map((data) => (data['icon'] as string) ?? 'construction')), {
    initialValue: 'construction'
  });
}
