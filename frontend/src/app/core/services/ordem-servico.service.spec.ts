import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { OrdemServicoRequest } from '../models/ordem-servico.model';
import { OrdemServicoService } from './ordem-servico.service';

const BASE_URL = `${environment.apiUrl}/ordens-servico`;

describe('OrdemServicoService', () => {
  let service: OrdemServicoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(OrdemServicoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('listar() envia os filtros de status/cliente/equipamento/período', () => {
    service
      .listar({ status: 'ABERTA', clienteId: 1, equipamentoId: 2, dataInicial: 'a', dataFinal: 'b' }, 0, 20)
      .subscribe();

    const req = httpMock.expectOne((r) => r.url === BASE_URL);
    expect(req.request.params.get('status')).toBe('ABERTA');
    expect(req.request.params.get('clienteId')).toBe('1');
    expect(req.request.params.get('equipamentoId')).toBe('2');
    expect(req.request.params.get('dataInicial')).toBe('a');
    expect(req.request.params.get('dataFinal')).toBe('b');
    req.flush({ content: [], pageNumber: 0, pageSize: 20, totalElements: 0, totalPages: 0, first: true, last: true });
  });

  it('salvar() faz POST /ordens-servico', () => {
    const dto: OrdemServicoRequest = { clienteId: 1, equipamentoId: 2, descricaoProblema: 'não gela' };
    service.salvar(dto).subscribe();

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('atualizarDiagnostico() faz PATCH com { diagnostico }', () => {
    service.atualizarDiagnostico(1, 'compressor queimado').subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1/diagnostico`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ diagnostico: 'compressor queimado' });
    req.flush({});
  });

  it('atualizarStatus() faz PATCH com { status }', () => {
    service.atualizarStatus(1, 'EM_ANDAMENTO').subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'EM_ANDAMENTO' });
    req.flush({});
  });

  it('listarHistorico() faz GET /ordens-servico/{id}/historico', () => {
    service.listarHistorico(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/historico`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('listarHistoricoDiagnostico() faz GET /ordens-servico/{id}/historico-diagnostico', () => {
    service.listarHistoricoDiagnostico(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/historico-diagnostico`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
