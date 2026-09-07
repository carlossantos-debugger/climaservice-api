import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ProdutoRequest } from '../models/produto.model';
import { ProdutoService } from './produto.service';

const BASE_URL = `${environment.apiUrl}/produtos`;

describe('ProdutoService', () => {
  let service: ProdutoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ProdutoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('listarTodos() faz GET /produtos', () => {
    service.listarTodos().subscribe();
    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('listarAtivos() faz GET /produtos/ativos', () => {
    service.listarAtivos().subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/ativos`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('salvar() faz POST /produtos', () => {
    const dto: ProdutoRequest = { nome: 'Filtro', valorPadrao: 40 };
    service.salvar(dto).subscribe();

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('atualizar() faz PUT /produtos/{id}', () => {
    const dto: ProdutoRequest = { nome: 'Filtro', valorPadrao: 40 };
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
