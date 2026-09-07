import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  OrcamentoFiltro,
  OrcamentoHistorico,
  OrcamentoItemAtualizarRequest,
  OrcamentoItemProdutoRequest,
  OrcamentoItemResponse,
  OrcamentoItemServicoRequest,
  OrcamentoRequest,
  OrcamentoResponse,
  StatusOrcamento
} from '../models/orcamento.model';
import { PageResponse } from '../models/page-response.model';

const BASE_URL = `${environment.apiUrl}/orcamentos`;

@Injectable({ providedIn: 'root' })
export class OrcamentoService {
  private readonly http = inject(HttpClient);

  criarParaOrdemServico(ordemServicoId: number, dto: OrcamentoRequest): Observable<OrcamentoResponse> {
    return this.http.post<OrcamentoResponse>(`${environment.apiUrl}/ordens-servico/${ordemServicoId}/orcamentos`, dto);
  }

  listarPorOrdemServico(ordemServicoId: number): Observable<OrcamentoResponse[]> {
    return this.http.get<OrcamentoResponse[]>(`${environment.apiUrl}/ordens-servico/${ordemServicoId}/orcamentos`);
  }

  listar(filtro: OrcamentoFiltro, page: number, size: number): Observable<PageResponse<OrcamentoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (filtro.status) {
      params = params.set('status', filtro.status);
    }
    if (filtro.dataInicial) {
      params = params.set('dataInicial', filtro.dataInicial);
    }
    if (filtro.dataFinal) {
      params = params.set('dataFinal', filtro.dataFinal);
    }

    return this.http.get<PageResponse<OrcamentoResponse>>(BASE_URL, { params });
  }

  buscarPorId(id: number): Observable<OrcamentoResponse> {
    return this.http.get<OrcamentoResponse>(`${BASE_URL}/${id}`);
  }

  atualizarStatus(id: number, status: StatusOrcamento): Observable<OrcamentoResponse> {
    return this.http.patch<OrcamentoResponse>(`${BASE_URL}/${id}/status`, { status });
  }

  listarItens(orcamentoId: number): Observable<OrcamentoItemResponse[]> {
    return this.http.get<OrcamentoItemResponse[]>(`${BASE_URL}/${orcamentoId}/itens`);
  }

  adicionarItemServico(orcamentoId: number, dto: OrcamentoItemServicoRequest): Observable<OrcamentoItemResponse> {
    return this.http.post<OrcamentoItemResponse>(`${BASE_URL}/${orcamentoId}/itens/servicos`, dto);
  }

  adicionarItemProduto(orcamentoId: number, dto: OrcamentoItemProdutoRequest): Observable<OrcamentoItemResponse> {
    return this.http.post<OrcamentoItemResponse>(`${BASE_URL}/${orcamentoId}/itens/produtos`, dto);
  }

  atualizarItem(
    orcamentoId: number,
    itemId: number,
    dto: OrcamentoItemAtualizarRequest
  ): Observable<OrcamentoItemResponse> {
    return this.http.put<OrcamentoItemResponse>(`${BASE_URL}/${orcamentoId}/itens/${itemId}`, dto);
  }

  removerItem(orcamentoId: number, itemId: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${orcamentoId}/itens/${itemId}`);
  }

  listarHistorico(id: number): Observable<OrcamentoHistorico[]> {
    return this.http.get<OrcamentoHistorico[]>(`${BASE_URL}/${id}/historico`);
  }
}
