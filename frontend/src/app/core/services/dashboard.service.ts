import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardFinanceiro, DashboardOperacional, DashboardResumo } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  resumo(): Observable<DashboardResumo> {
    return this.http.get<DashboardResumo>(`${environment.apiUrl}/dashboard/resumo`);
  }

  financeiro(): Observable<DashboardFinanceiro> {
    return this.http.get<DashboardFinanceiro>(`${environment.apiUrl}/dashboard/financeiro`);
  }

  operacional(): Observable<DashboardOperacional> {
    return this.http.get<DashboardOperacional>(`${environment.apiUrl}/dashboard/operacional`);
  }
}
