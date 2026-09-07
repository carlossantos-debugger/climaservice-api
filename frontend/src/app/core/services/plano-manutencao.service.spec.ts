import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { PlanoManutencaoRequest } from '../models/plano-manutencao.model';
import { PlanoManutencaoService } from './plano-manutencao.service';

const BASE_URL = `${environment.apiUrl}/planos-manutencao-preventiva`;

describe('PlanoManutencaoService', () => {
  let service: PlanoManutencaoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PlanoManutencaoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('listar() não é paginado e envia equipamentoId/ativo quando informados', () => {
    service.listar({ equipamentoId: 3, ativo: true }).subscribe();

    const req = httpMock.expectOne((r) => r.url === BASE_URL);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('equipamentoId')).toBe('3');
    expect(req.request.params.get('ativo')).toBe('true');
    req.flush([]);
  });

  it('listar() distingue ativo:false de ativo omitido', () => {
    service.listar({ ativo: false }).subscribe();

    const req = httpMock.expectOne((r) => r.url === BASE_URL);
    expect(req.request.params.get('ativo')).toBe('false');
    req.flush([]);
  });

  it('criar() faz POST no endpoint base', () => {
    const dto: PlanoManutencaoRequest = { equipamentoId: 3, intervaloMeses: 6 };
    service.criar(dto).subscribe();

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('atualizar() faz PUT /planos-manutencao-preventiva/{id}', () => {
    const dto = { intervaloMeses: 6, proximaExecucao: '2026-06-01' };
    service.atualizar(1, dto).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('gerarOrdemServico() faz POST /planos-manutencao-preventiva/{id}/gerar-ordem-servico', () => {
    service.gerarOrdemServico(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/gerar-ordem-servico`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('listarExecucoes() faz GET /planos-manutencao-preventiva/{id}/execucoes', () => {
    service.listarExecucoes(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/execucoes`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
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
