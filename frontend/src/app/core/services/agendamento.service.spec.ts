import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AgendamentoRequest } from '../models/agendamento.model';
import { AgendamentoService } from './agendamento.service';

const BASE_URL = `${environment.apiUrl}/agendamentos`;

describe('AgendamentoService', () => {
  let service: AgendamentoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AgendamentoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('listar() envia os filtros de período/técnico/status', () => {
    service.listar({ dataInicial: 'a', dataFinal: 'b', tecnicoId: 4, status: 'AGENDADO' }, 0, 20).subscribe();

    const req = httpMock.expectOne((r) => r.url === BASE_URL);
    expect(req.request.params.get('dataInicial')).toBe('a');
    expect(req.request.params.get('tecnicoId')).toBe('4');
    expect(req.request.params.get('status')).toBe('AGENDADO');
    req.flush({ content: [], pageNumber: 0, pageSize: 20, totalElements: 0, totalPages: 0, first: true, last: true });
  });

  it('criar() faz POST /agendamentos', () => {
    const dto: AgendamentoRequest = {
      ordemServicoId: 1,
      tecnicoId: 2,
      dataHoraInicio: '2026-01-01T10:00',
      dataHoraFim: '2026-01-01T11:00'
    };
    service.criar(dto).subscribe();

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('atualizarStatus() faz PATCH com { status }', () => {
    service.atualizarStatus(1, 'CONFIRMADO').subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'CONFIRMADO' });
    req.flush({});
  });

  it('reagendar() faz PATCH /agendamentos/{id}/reagendar com o corpo informado', () => {
    const dto = { dataHoraInicio: '2026-01-02T10:00', dataHoraFim: '2026-01-02T11:00' };
    service.reagendar(1, dto).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/1/reagendar`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('listarHistorico() faz GET /agendamentos/{id}/historico', () => {
    service.listarHistorico(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1/historico`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
