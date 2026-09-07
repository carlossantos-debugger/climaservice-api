import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { PagamentoRequest } from '../models/pagamento.model';
import { PagamentoService } from './pagamento.service';

const BASE_URL = `${environment.apiUrl}/pagamentos`;

describe('PagamentoService', () => {
  let service: PagamentoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PagamentoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('criarParaOrcamento() faz POST /orcamentos/{id}/pagamentos', () => {
    const dto: PagamentoRequest = { valor: 100, formaPagamento: 'PIX' };
    service.criarParaOrcamento(4, dto).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/orcamentos/4/pagamentos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('listarPorOrcamento() faz GET /orcamentos/{id}/pagamentos', () => {
    service.listarPorOrcamento(4).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/orcamentos/4/pagamentos`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('obterResumo() faz GET /orcamentos/{id}/pagamentos/resumo', () => {
    service.obterResumo(4).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/orcamentos/4/pagamentos/resumo`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('listar() envia os filtros de status/forma/período', () => {
    service.listar({ status: 'PENDENTE', formaPagamento: 'PIX', dataInicial: 'a' }, 0, 20).subscribe();

    const req = httpMock.expectOne((r) => r.url === BASE_URL);
    expect(req.request.params.get('status')).toBe('PENDENTE');
    expect(req.request.params.get('formaPagamento')).toBe('PIX');
    req.flush({ content: [], pageNumber: 0, pageSize: 20, totalElements: 0, totalPages: 0, first: true, last: true });
  });

  it('confirmar()/cancelar() fazem PATCH', () => {
    service.confirmar(1).subscribe();
    httpMock.expectOne(`${BASE_URL}/1/confirmar`).flush({});

    service.cancelar(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/cancelar`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('listarHistorico() faz GET /pagamentos/{id}/historico', () => {
    service.listarHistorico(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/historico`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
