import { HttpErrorResponse } from '@angular/common/http';

interface ApiErrorBody {
  message?: string;
  errors?: Record<string, string>;
}

/**
 * Extrait un message lisible depuis une erreur HTTP renvoyée par l'API.
 * L'API normalise ses erreurs sous la forme { message, errors? }.
 */
export function extractApiError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as ApiErrorBody | string | null;

    if (body && typeof body === 'object') {
      if (body.errors) {
        const first = Object.values(body.errors)[0];
        if (first) {
          return first;
        }
      }
      if (body.message) {
        return body.message;
      }
    }

    if (typeof body === 'string' && body.trim().length > 0) {
      return body;
    }

    if (error.status === 0) {
      return 'Serveur injoignable. Vérifiez votre connexion.';
    }
  }

  return 'Une erreur inattendue est survenue.';
}
