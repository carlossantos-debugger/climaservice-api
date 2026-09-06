import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  PlanoManutencaoAtualizarRequest,
  PlanoManutencaoExecucao,
  PlanoManutencaoFiltro,
  PlanoManutencaoRequest,
  PlanoManutencaoResponse
} from '../models/plano-manutencao.model';

const BASE_URL = `${environment.apiUrl}/planos-manutencao-preventiva`;

@Injectable({ providedIn: 'root' })
export class PlanoManutencaoService {
  private readonly http = inject(HttpClient);

  listar(filtro: PlanoManutencaoFiltro): Observable<PlanoManutencaoResponse[]> {
    let params = new HttpParams();

    if (filtro.equipamentoId) {
      params = params.set('equipamentoId', filtro.equipamentoId);
    }
    if (filtro.ativo !== undefined) {
      params = params.set('ativo', filtro.ativo);
    }

    return this.http.get<PlanoManutencaoResponse[]>(BASE_URL, { params });
  }

  buscarPorId(id: number): Observable<PlanoManutencaoResponse> {
    return this.http.get<PlanoManutencaoResponse>(`${BASE_URL}/${id}`);
  }

  criar(dto: PlanoManutencaoRequest): Observable<PlanoManutencaoResponse> {
    return this.http.post<PlanoManutencaoResponse>(BASE_URL, dto);
  }

  atualizar(id: number, dto: PlanoManutencaoAtualizarRequest): Observable<PlanoManutencaoResponse> {
    return this.http.put<PlanoManutencaoResponse>(`${BASE_URL}/${id}`, dto);
  }

  ativar(id: number): Observable<PlanoManutencaoResponse> {
    return this.http.patch<PlanoManutencaoResponse>(`${BASE_URL}/${id}/ativar`, {});
  }

  inativar(id: number): Observable<PlanoManutencaoResponse> {
    return this.http.patch<PlanoManutencaoResponse>(`${BASE_URL}/${id}/inativar`, {});
  }

  gerarOrdemServico(id: number): Observable<PlanoManutencaoExecucao> {
    return this.http.post<PlanoManutencaoExecucao>(`${BASE_URL}/${id}/gerar-ordem-servico`, {});
  }

  listarExecucoes(id: number): Observable<PlanoManutencaoExecucao[]> {
    return this.http.get<PlanoManutencaoExecucao[]>(`${BASE_URL}/${id}/execucoes`);
  }
}
