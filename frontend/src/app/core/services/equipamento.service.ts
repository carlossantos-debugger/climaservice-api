import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EquipamentoFiltro, EquipamentoRequest, EquipamentoResponse } from '../models/equipamento.model';
import { PageResponse } from '../models/page-response.model';

const BASE_URL = `${environment.apiUrl}/equipamentos`;

@Injectable({ providedIn: 'root' })
export class EquipamentoService {
  private readonly http = inject(HttpClient);

  listar(filtro: EquipamentoFiltro, page: number, size: number): Observable<PageResponse<EquipamentoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (filtro.clienteId) {
      params = params.set('clienteId', filtro.clienteId);
    }
    if (filtro.status) {
      params = params.set('status', filtro.status);
    }
    if (filtro.marca) {
      params = params.set('marca', filtro.marca);
    }
    if (filtro.modelo) {
      params = params.set('modelo', filtro.modelo);
    }

    return this.http.get<PageResponse<EquipamentoResponse>>(BASE_URL, { params });
  }

  buscarPorId(id: number): Observable<EquipamentoResponse> {
    return this.http.get<EquipamentoResponse>(`${BASE_URL}/${id}`);
  }

  salvar(dto: EquipamentoRequest): Observable<EquipamentoResponse> {
    return this.http.post<EquipamentoResponse>(BASE_URL, dto);
  }

  atualizar(id: number, dto: EquipamentoRequest): Observable<EquipamentoResponse> {
    return this.http.put<EquipamentoResponse>(`${BASE_URL}/${id}`, dto);
  }

  ativar(id: number): Observable<EquipamentoResponse> {
    return this.http.patch<EquipamentoResponse>(`${BASE_URL}/${id}/ativar`, {});
  }

  inativar(id: number): Observable<EquipamentoResponse> {
    return this.http.patch<EquipamentoResponse>(`${BASE_URL}/${id}/inativar`, {});
  }

  listarAtivosPorCliente(clienteId: number): Observable<EquipamentoResponse[]> {
    return this.http.get<EquipamentoResponse[]>(`${environment.apiUrl}/clientes/${clienteId}/equipamentos/ativos`);
  }
}
