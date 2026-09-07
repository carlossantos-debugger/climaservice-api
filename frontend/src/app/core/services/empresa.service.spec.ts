import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { EmpresaAtualizarRequest } from '../models/empresa.model';
import { EmpresaService } from './empresa.service';

const BASE_URL = `${environment.apiUrl}/empresa`;

describe('EmpresaService', () => {
  let service: EmpresaService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(EmpresaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('obterAtual() faz GET /empresa/me', () => {
    service.obterAtual().subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/me`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('atualizar() faz PATCH /empresa/me com o corpo completo (não é patch parcial de verdade)', () => {
    const dto: EmpresaAtualizarRequest = { nome: 'ClimaService LTDA', cpfCnpj: '12345678000199' };
    service.atualizar(dto).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/me`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });
});
