import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Usuario } from '../models/usuario.model';

/**
 * `GET /usuarios` é restrito a ADMIN (`SecurityConfig`: `/usuarios/**` -> hasRole("ADMIN")) —
 * um ATENDENTE criando um agendamento não consegue listar técnicos por aqui e recebe 403.
 * É uma limitação real do backend, não deste service; ver uso em AgendamentoForm.
 */
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);

  listarTodos(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${environment.apiUrl}/usuarios`);
  }
}
