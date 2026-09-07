import { fimDoDia, inicioDoDia } from './date-filtro.util';

describe('date-filtro.util', () => {
  it('inicioDoDia formata como YYYY-MM-DDT00:00:00', () => {
    expect(inicioDoDia(new Date(2026, 0, 5))).toBe('2026-01-05T00:00:00');
  });

  it('fimDoDia formata como YYYY-MM-DDT23:59:59', () => {
    expect(fimDoDia(new Date(2026, 0, 5))).toBe('2026-01-05T23:59:59');
  });

  it('preenche mês e dia com zero à esquerda quando necessário', () => {
    expect(inicioDoDia(new Date(2026, 2, 7))).toBe('2026-03-07T00:00:00');
  });
});
