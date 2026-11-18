import { FormControl, FormGroup } from '@angular/forms';
import { periodeCoherente } from './validateurs';

describe('periodeCoherente', () => {
  const groupe = (debut: string, fin: string) =>
    new FormGroup(
      { dateDebut: new FormControl(debut), dateFin: new FormControl(fin) },
      { validators: periodeCoherente('dateDebut', 'dateFin') },
    );

  it('accepte une date de début antérieure à la date de fin', () => {
    expect(groupe('2026-01-01', '2026-03-31').errors).toBeNull();
  });

  it('refuse des dates égales ou inversées (même règle que le backend)', () => {
    expect(groupe('2026-03-31', '2026-03-31').errors).toEqual({ periode: true });
    expect(groupe('2026-04-01', '2026-03-31').errors).toEqual({ periode: true });
  });

  it("ne dit rien tant qu'une des deux dates manque", () => {
    expect(groupe('', '2026-03-31').errors).toBeNull();
  });
});
