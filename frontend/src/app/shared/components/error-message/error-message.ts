import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-error-message',
  imports: [MatIconModule],
  templateUrl: './error-message.html',
  styleUrl: './error-message.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErrorMessage {
  /** Mensagem já pronta para exibição — normalmente extraída de ApiErrorResponse.message. */
  message = input.required<string>();
}
