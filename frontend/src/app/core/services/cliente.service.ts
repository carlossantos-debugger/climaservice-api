import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ClienteFiltro, ClienteRequest, ClienteResponse } from '../models/cliente.model';
import { PageResponse } from '../models/page-response.model';

const BASE_URL = `${environment.apiUrl}/clientes`;

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);

  listar(filtro: ClienteFiltro, page: number, size: number): Observable<PageResponse<ClienteResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (filtro.nome) {
      params = params.set('nome', filtro.nome);
    }
    if (filtro.cpfCnpj) {
      params = params.set('cpfCnpj', filtro.cpfCnpj);
    }

    return this.http.get<PageResponse<ClienteResponse>>(BASE_URL, { params });
  }

  buscarPorId(id: number): Observable<ClienteResponse> {
    return this.http.get<ClienteResponse>(`${BASE_URL}/${id}`);
  }

  salvar(dto: ClienteRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(BASE_URL, dto);
  }

  atualizar(id: number, dto: ClienteRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`${BASE_URL}/${id}`, dto);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${id}`);
  }
}
