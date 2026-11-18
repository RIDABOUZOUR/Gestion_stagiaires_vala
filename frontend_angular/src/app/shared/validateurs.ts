import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validateur de groupe : la date `debut` doit précéder strictement la date `fin`
 * (si les deux sont renseignées). Erreur posée sur le groupe : `{ periode: true }`.
 * Même règle que le backend (`dateDebut.isBefore(dateFin)`).
 */
export function periodeCoherente(debut: string, fin: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const valeurDebut = group.get(debut)?.value;
    const valeurFin = group.get(fin)?.value;
    return valeurDebut && valeurFin && valeurDebut >= valeurFin ? { periode: true } : null;
  };
}
