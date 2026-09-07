import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EmpresaAtualizarRequest, EmpresaResponse } from '../models/empresa.model';

const BASE_URL = `${environment.apiUrl}/empresa`;

@Injectable({ providedIn: 'root' })
export class EmpresaService {
  private readonly http = inject(HttpClient);

  obterAtual(): Observable<EmpresaResponse> {
    return this.http.get<EmpresaResponse>(`${BASE_URL}/me`);
  }

  atualizar(dto: EmpresaAtualizarRequest): Observable<EmpresaResponse> {
    return this.http.patch<EmpresaResponse>(`${BASE_URL}/me`, dto);
  }
}
