/**
 * Espelha `ApiErrorResponse` do `GlobalExceptionHandler` (404 e violação de regra de negócio / 400).
 */
export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

/**
 * Espelha `ValidationErrorResponse` do `GlobalExceptionHandler` (400 de validação de Bean Validation).
 * `fields` mapeia o nome do campo do DTO para a mensagem de validação, na mesma ordem em que
 * o backend as reporta.
 */
export interface ValidationErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  fields: Record<string, string>;
  path: string;
}

export function isValidationErrorResponse(
  body: unknown
): body is ValidationErrorResponse {
  return !!body && typeof body === 'object' && 'fields' in body;
}
