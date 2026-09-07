import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { DashboardService } from './dashboard.service';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('resumo() faz GET /dashboard/resumo', () => {
    service.resumo().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/dashboard/resumo`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('financeiro() faz GET /dashboard/financeiro', () => {
    service.financeiro().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/dashboard/financeiro`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('operacional() faz GET /dashboard/operacional', () => {
    service.operacional().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/dashboard/operacional`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
