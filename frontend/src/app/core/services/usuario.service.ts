import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TecnicoResumo, Usuario, UsuarioCadastroRequest } from '../models/usuario.model';

const BASE_URL = `${environment.apiUrl}/usuarios`;

/**
 * `GET /usuarios` é restrito a ADMIN (`SecurityConfig`: `/usuarios/**` -> hasRole("ADMIN")).
 * Para listar técnicos num seletor de formulário, use `listarTecnicos()` (GET /tecnicos) em vez
 * de `listarTodos()` — é aberto a ADMIN/ATENDENTE/TECNICO e já devolve só os ativos.
 */
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);

  listarTodos(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(BASE_URL);
  }

  listarTecnicos(): Observable<TecnicoResumo[]> {
    return this.http.get<TecnicoResumo[]>(`${environment.apiUrl}/tecnicos`);
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
