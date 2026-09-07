import { HttpErrorResponse } from '@angular/common/http';
import { extractErrorMessage } from './api-error.util';

function erroHttp(body: unknown, status = 400): HttpErrorResponse {
  return new HttpErrorResponse({ error: body, status });
}

describe('extractErrorMessage', () => {
  it('retorna o fallback quando o erro não é um HttpErrorResponse', () => {
    expect(extractErrorMessage(new Error('algo'), 'Fallback')).toBe('Fallback');
  });

  it('retorna o fallback padrão quando nenhum é informado', () => {
    expect(extractErrorMessage('qualquer coisa')).toBe(
      'Não foi possível completar a operação. Tente novamente em instantes.'
    );
  });

  it('extrai a primeira mensagem de um ValidationErrorResponse', () => {
    const erro = erroHttp({
      timestamp: '2026-01-01T00:00:00',
      status: 400,
      error: 'Bad Request',
      fields: { nome: 'O nome é obrigatório', email: 'E-mail inválido' },
      path: '/clientes'
    });

    expect(extractErrorMessage(erro)).toBe('O nome é obrigatório');
  });

  it('usa o fallback quando o ValidationErrorResponse não tem nenhum campo', () => {
    const erro = erroHttp({
      timestamp: '2026-01-01T00:00:00',
      status: 400,
      error: 'Bad Request',
      fields: {},
      path: '/clientes'
    });

    expect(extractErrorMessage(erro, 'Fallback')).toBe('Fallback');
  });

  it('extrai a mensagem de um ApiErrorResponse simples', () => {
    const erro = erroHttp({
      timestamp: '2026-01-01T00:00:00',
      status: 404,
      error: 'Not Found',
      message: 'Cliente não encontrado',
      path: '/clientes/999'
    });

    expect(extractErrorMessage(erro)).toBe('Cliente não encontrado');
  });

  it('usa o fallback quando o corpo do erro não tem o formato esperado', () => {
    const erro = erroHttp('erro de texto puro sem estrutura');

    expect(extractErrorMessage(erro, 'Fallback')).toBe('Fallback');
  });

  it('usa o fallback quando não há corpo de erro (ex.: status 0, sem conexão)', () => {
    const erro = erroHttp(null, 0);

    expect(extractErrorMessage(erro, 'Sem conexão')).toBe('Sem conexão');
  });
});
