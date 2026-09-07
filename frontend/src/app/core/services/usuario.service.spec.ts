import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { UsuarioCadastroRequest } from '../models/usuario.model';
import { UsuarioService } from './usuario.service';

const BASE_URL = `${environment.apiUrl}/usuarios`;

describe('UsuarioService', () => {
  let service: UsuarioService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(UsuarioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('listarTodos() faz GET /usuarios', () => {
    service.listarTodos().subscribe();
    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('buscarPorId() faz GET /usuarios/{id}', () => {
    service.buscarPorId(1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/1`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('cadastrar() faz POST /usuarios com o corpo informado', () => {
    const dto: UsuarioCadastroRequest = { nome: 'Ana', email: 'ana@e.com', senha: 'senha1234', role: 'TECNICO' };
    service.cadastrar(dto).subscribe();

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('listarTecnicos() faz GET /tecnicos', () => {
    service.listarTecnicos().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/tecnicos`);
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
