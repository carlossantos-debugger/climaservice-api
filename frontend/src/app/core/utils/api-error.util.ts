import { HttpErrorResponse } from '@angular/common/http';
import { isValidationErrorResponse } from '../models/api-error.model';

const FALLBACK_MESSAGE = 'Não foi possível completar a operação. Tente novamente em instantes.';

/**
 * Extrai uma mensagem exibível de um erro HTTP, priorizando o texto que o próprio
 * backend devolveu (ApiErrorResponse.message ou o primeiro campo de
 * ValidationErrorResponse.fields) — nunca inventamos uma mensagem quando o backend já
 * mandou uma.
 */
export function extractErrorMessage(error: unknown, fallback = FALLBACK_MESSAGE): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  const body: unknown = error.error;

  if (isValidationErrorResponse(body)) {
    return Object.values(body.fields)[0] ?? fallback;
  }

  if (body && typeof body === 'object' && 'message' in body && typeof body.message === 'string') {
    return body.message;
  }

  return fallback;
}
