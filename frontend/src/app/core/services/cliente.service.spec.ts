import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ClienteRequest } from '../models/cliente.model';
import { ClienteService } from './cliente.service';

const BASE_URL = `${environment.apiUrl}/clientes`;

describe('ClienteService', () => {
  let service: ClienteService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ClienteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('listar() envia page/size e só inclui filtros informados', () => {
    service.listar({ nome: 'joão' }, 1, 20).subscribe();

    const req = httpMock.expectOne((r) => r.url === BASE_URL);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('20');
    expect(req.request.params.get('nome')).toBe('joão');
    expect(req.request.params.has('cpfCnpj')).toBe(false);
    req.flush({ content: [], pageNumber: 1, pageSize: 20, totalElements: 0, totalPages: 0, first: true, last: true });
  });

  it('listar() não envia nenhum filtro quando nenhum foi informado', () => {
    service.listar({}, 0, 20).subscribe();

    const req = httpMock.expectOne((r) => r.url === BASE_URL);
    expect(req.request.params.has('nome')).toBe(false);
    expect(req.request.params.has('cpfCnpj')).toBe(false);
    req.flush({ content: [], pageNumber: 0, pageSize: 20, totalElements: 0, totalPages: 0, first: true, last: true });
  });

  it('buscarPorId() faz GET /clientes/{id}', () => {
    service.buscarPorId(5).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/5`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('salvar() faz POST /clientes com o corpo informado', () => {
    const dto: ClienteRequest = { nome: 'João', cpfCnpj: '12345678901' };
    service.salvar(dto).subscribe();

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('atualizar() faz PUT /clientes/{id}', () => {
    const dto: ClienteRequest = { nome: 'João', cpfCnpj: '12345678901' };
    service.atualizar(5, dto).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/5`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('excluir() faz DELETE /clientes/{id}', () => {
    service.excluir(5).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
