import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Usuario, UsuarioCadastroRequest } from '../models/usuario.model';

const BASE_URL = `${environment.apiUrl}/usuarios`;

/**
 * `GET /usuarios` é restrito a ADMIN (`SecurityConfig`: `/usuarios/**` -> hasRole("ADMIN")) —
 * um ATENDENTE criando um agendamento não consegue listar técnicos por aqui e recebe 403.
 * É uma limitação real do backend, não deste service; ver uso em AgendamentoForm/PlanoForm.
 */
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);

  listarTodos(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(BASE_URL);
  }

  buscarPorId(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${BASE_URL}/${id}`);
  }

  cadastrar(dto: UsuarioCadastroRequest): Observable<Usuario> {
    return this.http.post<Usuario>(BASE_URL, dto);
  }

  ativar(id: number): Observable<Usuario> {
    return this.http.patch<Usuario>(`${BASE_URL}/${id}/ativar`, {});
  }

  inativar(id: number): Observable<Usuario> {
    return this.http.patch<Usuario>(`${BASE_URL}/${id}/inativar`, {});
  }
}
