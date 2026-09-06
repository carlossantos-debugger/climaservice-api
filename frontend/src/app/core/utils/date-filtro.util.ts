/**
 * Converte um `Date` de um mat-datepicker num filtro de período `LocalDateTime` (ISO,
 * sem timezone) para os endpoints de listagem que aceitam `dataInicial`/`dataFinal` —
 * usado por ordens-servico e agendamentos.
 */
export function inicioDoDia(data: Date): string {
  return `${dataParaISO(data)}T00:00:00`;
}

export function fimDoDia(data: Date): string {
  return `${dataParaISO(data)}T23:59:59`;
}

function dataParaISO(data: Date): string {
  const ano = data.getFullYear();
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${ano}-${mes}-${dia}`;
}
