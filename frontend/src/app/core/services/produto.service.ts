import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProdutoRequest, ProdutoResponse } from '../models/produto.model';

const BASE_URL = `${environment.apiUrl}/produtos`;

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private readonly http = inject(HttpClient);

  listarTodos(): Observable<ProdutoResponse[]> {
    return this.http.get<ProdutoResponse[]>(BASE_URL);
  }

  listarAtivos(): Observable<ProdutoResponse[]> {
    return this.http.get<ProdutoResponse[]>(`${BASE_URL}/ativos`);
  }

  buscarPorId(id: number): Observable<ProdutoResponse> {
    return this.http.get<ProdutoResponse>(`${BASE_URL}/${id}`);
  }

  salvar(dto: ProdutoRequest): Observable<ProdutoResponse> {
    return this.http.post<ProdutoResponse>(BASE_URL, dto);
  }

  atualizar(id: number, dto: ProdutoRequest): Observable<ProdutoResponse> {
    return this.http.put<ProdutoResponse>(`${BASE_URL}/${id}`, dto);
  }

  ativar(id: number): Observable<ProdutoResponse> {
    return this.http.patch<ProdutoResponse>(`${BASE_URL}/${id}/ativar`, {});
  }

  inativar(id: number): Observable<ProdutoResponse> {
    return this.http.patch<ProdutoResponse>(`${BASE_URL}/${id}/inativar`, {});
  }
}
