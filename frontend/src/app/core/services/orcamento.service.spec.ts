import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { OrcamentoService } from './orcamento.service';

const BASE_URL = `${environment.apiUrl}/orcamentos`;

describe('OrcamentoService', () => {
  let service: OrcamentoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(OrcamentoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('criarParaOrdemServico() faz POST /ordens-servico/{id}/orcamentos', () => {
    service.criarParaOrdemServico(9, { observacao: 'urgente' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/ordens-servico/9/orcamentos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ observacao: 'urgente' });
    req.flush({});
  });

  it('listarPorOrdemServico() faz GET /ordens-servico/{id}/orcamentos', () => {
    service.listarPorOrdemServico(9).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/ordens-servico/9/orcamentos`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('listar() envia os filtros de status/período', () => {
    service.listar({ status: 'ENVIADO', dataInicial: 'a', dataFinal: 'b' }, 0, 20).subscribe();

    const req = httpMock.expectOne((r) => r.url === BASE_URL);
    expect(req.request.params.get('status')).toBe('ENVIADO');
    expect(req.request.params.get('dataInicial')).toBe('a');
    req.flush({ content: [], pageNumber: 0, pageSize: 20, totalElements: 0, totalPages: 0, first: true, last: true });
  });

  it('atualizarStatus() faz PATCH com { status }', () => {
    service.atualizarStatus(1, 'APROVADO').subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'APROVADO' });
    req.flush({});
  });

  it('adicionarItemServico() faz POST /orcamentos/{id}/itens/servicos', () => {
    const dto = { servicoId: 2, quantidade: 1 };
    service.adicionarItemServico(1, dto).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1/itens/servicos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('adicionarItemProduto() faz POST /orcamentos/{id}/itens/produtos', () => {
    const dto = { produtoId: 3, quantidade: 2 };
    service.adicionarItemProduto(1, dto).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1/itens/produtos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('atualizarItem() faz PUT /orcamentos/{id}/itens/{itemId}', () => {
    const dto = { quantidade: 3, valorUnitario: 10 };
    service.atualizarItem(1, 7, dto).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1/itens/7`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('removerItem() faz DELETE /orcamentos/{id}/itens/{itemId}', () => {
    service.removerItem(1, 7).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/itens/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('listarItens()/listarHistorico() fazem GET', () => {
    service.listarItens(1).subscribe();
    httpMock.expectOne(`${BASE_URL}/1/itens`).flush([]);

    service.listarHistorico(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/historico`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
