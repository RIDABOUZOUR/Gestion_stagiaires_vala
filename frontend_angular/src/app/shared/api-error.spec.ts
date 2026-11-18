import { HttpErrorResponse } from '@angular/common/http';
import { extractApiError } from './api-error';

describe('extractApiError', () => {
  const erreur = (error: unknown, status = 400) => new HttpErrorResponse({ error, status });

  it("privilégie le premier message de champ du format d'erreur de l'API", () => {
    expect(extractApiError(erreur({ message: 'Erreur de validation', errors: { nom: 'Le nom est obligatoire' } })))
      .toBe('Le nom est obligatoire');
  });

  it('renvoie le message général sinon', () => {
    expect(extractApiError(erreur({ message: 'Impossible de supprimer cet encadrant.' }, 409)))
      .toBe('Impossible de supprimer cet encadrant.');
  });

  it('accepte un corps texte', () => {
    expect(extractApiError(erreur('Texte brut'))).toBe('Texte brut');
  });

  it('signale un serveur injoignable (status 0)', () => {
    expect(extractApiError(erreur(null, 0))).toBe('Serveur injoignable. Vérifiez votre connexion.');
  });

  it('a un message par défaut pour toute autre erreur', () => {
    expect(extractApiError(new Error('boom'))).toBe('Une erreur inattendue est survenue.');
  });
});
