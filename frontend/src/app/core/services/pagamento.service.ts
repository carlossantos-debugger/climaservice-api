import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  PagamentoFiltro,
  PagamentoHistorico,
  PagamentoRequest,
  PagamentoResponse,
  PagamentoResumo
} from '../models/pagamento.model';
import { PageResponse } from '../models/page-response.model';

const BASE_URL = `${environment.apiUrl}/pagamentos`;

@Injectable({ providedIn: 'root' })
export class PagamentoService {
  private readonly http = inject(HttpClient);

  criarParaOrcamento(orcamentoId: number, dto: PagamentoRequest): Observable<PagamentoResponse> {
    return this.http.post<PagamentoResponse>(`${environment.apiUrl}/orcamentos/${orcamentoId}/pagamentos`, dto);
  }

  listarPorOrcamento(orcamentoId: number): Observable<PagamentoResponse[]> {
    return this.http.get<PagamentoResponse[]>(`${environment.apiUrl}/orcamentos/${orcamentoId}/pagamentos`);
  }

  obterResumo(orcamentoId: number): Observable<PagamentoResumo> {
    return this.http.get<PagamentoResumo>(`${environment.apiUrl}/orcamentos/${orcamentoId}/pagamentos/resumo`);
  }

  listar(filtro: PagamentoFiltro, page: number, size: number): Observable<PageResponse<PagamentoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (filtro.status) {
      params = params.set('status', filtro.status);
    }
    if (filtro.formaPagamento) {
      params = params.set('formaPagamento', filtro.formaPagamento);
    }
    if (filtro.dataInicial) {
      params = params.set('dataInicial', filtro.dataInicial);
    }
    if (filtro.dataFinal) {
      params = params.set('dataFinal', filtro.dataFinal);
    }

    return this.http.get<PageResponse<PagamentoResponse>>(BASE_URL, { params });
  }

  buscarPorId(id: number): Observable<PagamentoResponse> {
    return this.http.get<PagamentoResponse>(`${BASE_URL}/${id}`);
  }

  confirmar(id: number): Observable<PagamentoResponse> {
    return this.http.patch<PagamentoResponse>(`${BASE_URL}/${id}/confirmar`, {});
  }

  cancelar(id: number): Observable<PagamentoResponse> {
    return this.http.patch<PagamentoResponse>(`${BASE_URL}/${id}/cancelar`, {});
  }

  listarHistorico(id: number): Observable<PagamentoHistorico[]> {
    return this.http.get<PagamentoHistorico[]>(`${BASE_URL}/${id}/historico`);
  }
}
