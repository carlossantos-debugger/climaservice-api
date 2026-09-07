import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { ClienteResponse } from '../../../core/models/cliente.model';
import { PageResponse } from '../../../core/models/page-response.model';
import { ConfirmDialogService } from '../../../shared/components/confirm-dialog/confirm-dialog.service';
import { ClienteList } from './cliente-list';

const CLIENTE: ClienteResponse = { id: 1, nome: 'João', cpfCnpj: '12345678901' };

function paginaCom(clientes: ClienteResponse[], totalElements = clientes.length): PageResponse<ClienteResponse> {
  return { content: clientes, pageNumber: 0, pageSize: 20, totalElements, totalPages: 1, first: true, last: true };
}

describe('ClienteList', () => {
  let fixture: ComponentFixture<ClienteList>;
  let component: ClienteList;
  let clienteService: { listar: ReturnType<typeof vi.fn>; excluir: ReturnType<typeof vi.fn> };
  let confirmDialog: { confirm: ReturnType<typeof vi.fn> };
  let authService: { hasRole: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    clienteService = { listar: vi.fn(() => of(paginaCom([CLIENTE]))), excluir: vi.fn(() => of(undefined)) };
    confirmDialog = { confirm: vi.fn(() => of(true)) };
    authService = { hasRole: vi.fn(() => true) };

    TestBed.configureTestingModule({
      imports: [ClienteList],
      providers: [
        provideRouter([]),
        { provide: ClienteService, useValue: clienteService },
        { provide: ConfirmDialogService, useValue: confirmDialog },
        { provide: AuthService, useValue: authService }
      ]
    });

    fixture = TestBed.createComponent(ClienteList);
    component = fixture.componentInstance;
  });

  it('carrega a primeira página de clientes ao iniciar', () => {
    fixture.detectChanges();

    expect(clienteService.listar).toHaveBeenCalledWith({ nome: '', cpfCnpj: '' }, 0, 20);
    expect(component.clientes()).toEqual([CLIENTE]);
    expect(component.totalElements()).toBe(1);
    expect(component.loading()).toBe(false);
  });

  it('exibe uma mensagem de erro quando a listagem falha', () => {
    clienteService.listar.mockReturnValue(throwError(() => new Error('falhou')));

    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.errorMessage()).toBe('Não foi possível carregar os clientes.');
  });

  it('buscar() volta para a primeira página e reaplica os filtros', () => {
    fixture.detectChanges();
    component.pageIndex.set(2);
    component.filtroForm.setValue({ nome: 'joão', cpfCnpj: '' });
    clienteService.listar.mockClear();

    component.buscar();

    expect(component.pageIndex()).toBe(0);
    expect(clienteService.listar).toHaveBeenCalledWith({ nome: 'joão', cpfCnpj: '' }, 0, 20);
  });

  it('onPage() atualiza página/tamanho e recarrega', () => {
    fixture.detectChanges();
    clienteService.listar.mockClear();

    component.onPage({ pageIndex: 1, pageSize: 50, length: 1 });

    expect(component.pageIndex()).toBe(1);
    expect(component.pageSize()).toBe(50);
    expect(clienteService.listar).toHaveBeenCalledWith({ nome: '', cpfCnpj: '' }, 1, 50);
  });

  it('excluir() só chama o backend quando o usuário confirma', () => {
    fixture.detectChanges();
    confirmDialog.confirm.mockReturnValue(of(false));

    component.excluir(CLIENTE);

    expect(clienteService.excluir).not.toHaveBeenCalled();
  });

  it('excluir() chama o backend e recarrega a lista quando confirmado', () => {
    fixture.detectChanges();
    clienteService.listar.mockClear();

    component.excluir(CLIENTE);

    expect(clienteService.excluir).toHaveBeenCalledWith(CLIENTE.id);
    expect(clienteService.listar).toHaveBeenCalled();
  });

  it('podeEditar/podeExcluir são true para um ADMIN', () => {
    authService.hasRole.mockImplementation((...roles: string[]) => roles.includes('ADMIN'));
    fixture.detectChanges();

    expect(component.podeEditar()).toBe(true);
    expect(component.podeExcluir()).toBe(true);
  });

  it('podeExcluir é false para um ATENDENTE (só ADMIN exclui)', () => {
    authService.hasRole.mockImplementation((...roles: string[]) => roles.includes('ATENDENTE'));
    fixture.detectChanges();

    expect(component.podeEditar()).toBe(true);
    expect(component.podeExcluir()).toBe(false);
  });
});
