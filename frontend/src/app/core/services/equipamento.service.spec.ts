import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { EquipamentoRequest } from '../models/equipamento.model';
import { EquipamentoService } from './equipamento.service';

const BASE_URL = `${environment.apiUrl}/equipamentos`;

describe('EquipamentoService', () => {
  let service: EquipamentoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(EquipamentoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('listar() envia os filtros informados como query params', () => {
    service.listar({ clienteId: 3, status: 'ATIVO', marca: 'LG', modelo: 'Split' }, 0, 20).subscribe();

    const req = httpMock.expectOne((r) => r.url === BASE_URL);
    expect(req.request.params.get('clienteId')).toBe('3');
    expect(req.request.params.get('status')).toBe('ATIVO');
    expect(req.request.params.get('marca')).toBe('LG');
    expect(req.request.params.get('modelo')).toBe('Split');
    req.flush({ content: [], pageNumber: 0, pageSize: 20, totalElements: 0, totalPages: 0, first: true, last: true });
  });

  it('buscarPorId() faz GET /equipamentos/{id}', () => {
    service.buscarPorId(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('salvar() faz POST /equipamentos', () => {
    const dto: EquipamentoRequest = { marca: 'LG', modelo: 'Split', capacidadeBtu: 12000, clienteId: 3 };
    service.salvar(dto).subscribe();

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('atualizar() faz PUT /equipamentos/{id}', () => {
    const dto: EquipamentoRequest = { marca: 'LG', modelo: 'Split', capacidadeBtu: 12000, clienteId: 3 };
    service.atualizar(1, dto).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('ativar() faz PATCH /equipamentos/{id}/ativar', () => {
    service.ativar(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/ativar`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('inativar() faz PATCH /equipamentos/{id}/inativar', () => {
    service.inativar(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/inativar`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('listarAtivosPorCliente() faz GET /clientes/{id}/equipamentos/ativos', () => {
    service.listarAtivosPorCliente(3).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/clientes/3/equipamentos/ativos`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
