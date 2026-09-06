import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AgendamentoFiltro,
  AgendamentoHistorico,
  AgendamentoReagendarRequest,
  AgendamentoRequest,
  AgendamentoResponse,
  StatusAgendamento
} from '../models/agendamento.model';
import { PageResponse } from '../models/page-response.model';

const BASE_URL = `${environment.apiUrl}/agendamentos`;

@Injectable({ providedIn: 'root' })
export class AgendamentoService {
  private readonly http = inject(HttpClient);

  listar(filtro: AgendamentoFiltro, page: number, size: number): Observable<PageResponse<AgendamentoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (filtro.dataInicial) {
      params = params.set('dataInicial', filtro.dataInicial);
    }
    if (filtro.dataFinal) {
      params = params.set('dataFinal', filtro.dataFinal);
    }
    if (filtro.tecnicoId) {
      params = params.set('tecnicoId', filtro.tecnicoId);
    }
    if (filtro.status) {
      params = params.set('status', filtro.status);
    }

    return this.http.get<PageResponse<AgendamentoResponse>>(BASE_URL, { params });
  }

  buscarPorId(id: number): Observable<AgendamentoResponse> {
    return this.http.get<AgendamentoResponse>(`${BASE_URL}/${id}`);
  }

  criar(dto: AgendamentoRequest): Observable<AgendamentoResponse> {
    return this.http.post<AgendamentoResponse>(BASE_URL, dto);
  }

  atualizarStatus(id: number, status: StatusAgendamento): Observable<AgendamentoResponse> {
    return this.http.patch<AgendamentoResponse>(`${BASE_URL}/${id}/status`, { status });
  }

  reagendar(id: number, dto: AgendamentoReagendarRequest): Observable<AgendamentoResponse> {
    return this.http.patch<AgendamentoResponse>(`${BASE_URL}/${id}/reagendar`, dto);
  }

  listarHistorico(id: number): Observable<AgendamentoHistorico[]> {
    return this.http.get<AgendamentoHistorico[]>(`${BASE_URL}/${id}/historico`);
  }
}
