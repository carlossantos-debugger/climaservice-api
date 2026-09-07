import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ServicoRequest, ServicoResponse } from '../models/servico.model';

const BASE_URL = `${environment.apiUrl}/servicos`;

@Injectable({ providedIn: 'root' })
export class ServicoService {
  private readonly http = inject(HttpClient);

  listarTodos(): Observable<ServicoResponse[]> {
    return this.http.get<ServicoResponse[]>(BASE_URL);
  }

  listarAtivos(): Observable<ServicoResponse[]> {
    return this.http.get<ServicoResponse[]>(`${BASE_URL}/ativos`);
  }

  buscarPorId(id: number): Observable<ServicoResponse> {
    return this.http.get<ServicoResponse>(`${BASE_URL}/${id}`);
  }

  salvar(dto: ServicoRequest): Observable<ServicoResponse> {
    return this.http.post<ServicoResponse>(BASE_URL, dto);
  }

  atualizar(id: number, dto: ServicoRequest): Observable<ServicoResponse> {
    return this.http.put<ServicoResponse>(`${BASE_URL}/${id}`, dto);
  }

  ativar(id: number): Observable<ServicoResponse> {
    return this.http.patch<ServicoResponse>(`${BASE_URL}/${id}/ativar`, {});
  }

  inativar(id: number): Observable<ServicoResponse> {
    return this.http.patch<ServicoResponse>(`${BASE_URL}/${id}/inativar`, {});
  }
}
