import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  OrdemServicoDiagnosticoHistorico,
  OrdemServicoFiltro,
  OrdemServicoHistorico,
  OrdemServicoRequest,
  OrdemServicoResponse,
  StatusOrdemServico
} from '../models/ordem-servico.model';
import { PageResponse } from '../models/page-response.model';

const BASE_URL = `${environment.apiUrl}/ordens-servico`;

@Injectable({ providedIn: 'root' })
export class OrdemServicoService {
  private readonly http = inject(HttpClient);

  listar(filtro: OrdemServicoFiltro, page: number, size: number): Observable<PageResponse<OrdemServicoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (filtro.status) {
      params = params.set('status', filtro.status);
    }
    if (filtro.clienteId) {
      params = params.set('clienteId', filtro.clienteId);
    }
    if (filtro.equipamentoId) {
      params = params.set('equipamentoId', filtro.equipamentoId);
    }
    if (filtro.dataInicial) {
      params = params.set('dataInicial', filtro.dataInicial);
    }
    if (filtro.dataFinal) {
      params = params.set('dataFinal', filtro.dataFinal);
    }

    return this.http.get<PageResponse<OrdemServicoResponse>>(BASE_URL, { params });
  }

  buscarPorId(id: number): Observable<OrdemServicoResponse> {
    return this.http.get<OrdemServicoResponse>(`${BASE_URL}/${id}`);
  }

  salvar(dto: OrdemServicoRequest): Observable<OrdemServicoResponse> {
    return this.http.post<OrdemServicoResponse>(BASE_URL, dto);
  }

  atualizarDiagnostico(id: number, diagnostico: string): Observable<OrdemServicoResponse> {
    return this.http.patch<OrdemServicoResponse>(`${BASE_URL}/${id}/diagnostico`, { diagnostico });
  }

  atualizarStatus(id: number, status: StatusOrdemServico): Observable<OrdemServicoResponse> {
    return this.http.patch<OrdemServicoResponse>(`${BASE_URL}/${id}/status`, { status });
  }

  listarHistorico(id: number): Observable<OrdemServicoHistorico[]> {
    return this.http.get<OrdemServicoHistorico[]>(`${BASE_URL}/${id}/historico`);
  }

  listarHistoricoDiagnostico(id: number): Observable<OrdemServicoDiagnosticoHistorico[]> {
    return this.http.get<OrdemServicoDiagnosticoHistorico[]>(`${BASE_URL}/${id}/historico-diagnostico`);
  }
}
