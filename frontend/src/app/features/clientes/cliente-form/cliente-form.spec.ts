import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ClienteService } from '../../../core/services/cliente.service';
import { ClienteResponse } from '../../../core/models/cliente.model';
import { ClienteForm } from './cliente-form';

describe('ClienteForm', () => {
  let fixture: ComponentFixture<ClienteForm>;
  let component: ClienteForm;
  let clienteService: {
    buscarPorId: ReturnType<typeof vi.fn>;
    salvar: ReturnType<typeof vi.fn>;
    atualizar: ReturnType<typeof vi.fn>;
  };
  let navigateSpy: ReturnType<typeof vi.fn>;

  function configurar(id: string | null) {
    clienteService = { buscarPorId: vi.fn(), salvar: vi.fn(), atualizar: vi.fn() };

    TestBed.configureTestingModule({
      imports: [ClienteForm],
      providers: [
        provideRouter([]),
        { provide: ClienteService, useValue: clienteService },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap(id ? { id } : {}) } } }
      ]
    });

    fixture = TestBed.createComponent(ClienteForm);
    component = fixture.componentInstance;
    // Suprime a navegação real (não há rotas configuradas no teste) — só interessa que foi chamada.
    navigateSpy = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
  }

  describe('modo criação (sem id na rota)', () => {
    beforeEach(() => configurar(null));

    it('não busca nenhum cliente e começa em modo criação', () => {
      fixture.detectChanges();

      expect(clienteService.buscarPorId).not.toHaveBeenCalled();
      expect(component.modoEdicao()).toBe(false);
    });

    it('não submete e marca os campos como tocados quando o formulário é inválido', () => {
      fixture.detectChanges();

      component.submit();

      expect(clienteService.salvar).not.toHaveBeenCalled();
      expect(component.form.controls.nome.touched).toBe(true);
    });

    it('rejeita um CPF/CNPJ que não tem 11 nem 14 dígitos', () => {
      fixture.detectChanges();
      component.form.controls.cpfCnpj.setValue('123');
      expect(component.form.controls.cpfCnpj.invalid).toBe(true);

      component.form.controls.cpfCnpj.setValue('12345678901');
      expect(component.form.controls.cpfCnpj.hasError('pattern')).toBe(false);
    });

    it('em submit válido, chama salvar() e navega para /clientes', () => {
      fixture.detectChanges();
      clienteService.salvar.mockReturnValue(of({} as ClienteResponse));

      component.form.controls.nome.setValue('João');
      component.form.controls.cpfCnpj.setValue('12345678901');
      component.submit();

      expect(clienteService.salvar).toHaveBeenCalledWith(
        expect.objectContaining({ nome: 'João', cpfCnpj: '12345678901' })
      );
      expect(navigateSpy).toHaveBeenCalledWith('/clientes');
    });

    it('em erro no submit, exibe a mensagem e libera o formulário', () => {
      fixture.detectChanges();
      clienteService.salvar.mockReturnValue(throwError(() => new Error('falhou')));

      component.form.controls.nome.setValue('João');
      component.form.controls.cpfCnpj.setValue('12345678901');
      component.submit();

      expect(component.saving()).toBe(false);
      expect(component.errorMessage()).toBe('Não foi possível salvar o cliente.');
    });

    it('cancelar() navega para /clientes', () => {
      fixture.detectChanges();

      component.cancelar();

      expect(navigateSpy).toHaveBeenCalledWith('/clientes');
    });
  });

  describe('modo edição (com id na rota)', () => {
    beforeEach(() => configurar('5'));

    it('busca o cliente por id e preenche o formulário', () => {
      const cliente: ClienteResponse = { id: 5, nome: 'Maria', cpfCnpj: '98765432100' };
      clienteService.buscarPorId.mockReturnValue(of(cliente));

      fixture.detectChanges();

      expect(clienteService.buscarPorId).toHaveBeenCalledWith(5);
      expect(component.modoEdicao()).toBe(true);
      expect(component.form.controls.nome.value).toBe('Maria');
    });

    it('trata endereco null do backend sem lançar erro (fallback para {})', () => {
      const cliente: ClienteResponse = { id: 5, nome: 'Maria', cpfCnpj: '98765432100', endereco: undefined };
      clienteService.buscarPorId.mockReturnValue(of(cliente));

      expect(() => fixture.detectChanges()).not.toThrow();
      expect(component.form.get('endereco.cidade')?.value).toBe('');
    });

    it('em submit válido, chama atualizar(id, dto) em vez de salvar()', () => {
      const cliente: ClienteResponse = { id: 5, nome: 'Maria', cpfCnpj: '98765432100' };
      clienteService.buscarPorId.mockReturnValue(of(cliente));
      clienteService.atualizar.mockReturnValue(of(cliente));
      fixture.detectChanges();

      component.submit();

      expect(clienteService.atualizar).toHaveBeenCalledWith(5, expect.objectContaining({ nome: 'Maria' }));
      expect(clienteService.salvar).not.toHaveBeenCalled();
    });
  });
});
