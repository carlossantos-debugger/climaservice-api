import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';
import { ConfirmDialog, ConfirmDialogData } from './confirm-dialog';

/**
 * Evita repetir `MatDialog.open(ConfirmDialog, ...)` em cada tela que precisa
 * confirmar uma ação destrutiva (excluir cliente, cancelar OS, etc.) — praticamente toda
 * feature a partir da branch 4 vai precisar disso.
 */
@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private readonly dialog = inject(MatDialog);

  /** Emite `true`/`false` conforme a escolha do usuário e completa em seguida. */
  confirm(data: ConfirmDialogData): Observable<boolean> {
    return this.dialog
      .open(ConfirmDialog, { data, width: '420px' })
      .afterClosed()
      .pipe(map((result) => result === true));
  }
}
