import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ServicoRequest } from '../models/servico.model';
import { ServicoService } from './servico.service';

const BASE_URL = `${environment.apiUrl}/servicos`;

describe('ServicoService', () => {
  let service: ServicoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ServicoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('listarTodos() faz GET /servicos', () => {
    service.listarTodos().subscribe();
    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('listarAtivos() faz GET /servicos/ativos', () => {
    service.listarAtivos().subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/ativos`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('salvar() faz POST /servicos', () => {
    const dto: ServicoRequest = { nome: 'Limpeza', valorPadrao: 150 };
    service.salvar(dto).subscribe();

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('atualizar() faz PUT /servicos/{id}', () => {
    const dto: ServicoRequest = { nome: 'Limpeza', valorPadrao: 150 };
    service.atualizar(1, dto).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('ativar()/inativar() fazem PATCH', () => {
    service.ativar(1).subscribe();
    httpMock.expectOne(`${BASE_URL}/1/ativar`).flush({});

    service.inativar(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/inativar`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });
});
