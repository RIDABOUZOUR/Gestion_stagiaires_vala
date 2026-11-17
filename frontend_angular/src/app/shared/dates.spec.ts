import { ajouterJours, decalerMois, isoDate, jourSemaine, joursDansLeMois, lundiDe, moisDe } from './dates';

describe('dates', () => {
  it('construit et décale des dates ISO sans effet de fuseau horaire', () => {
    expect(isoDate(2026, 3, 1)).toBe('2026-03-01');
    expect(moisDe('2026-03-15')).toBe('2026-03');
    expect(decalerMois('2026-12', 1)).toBe('2027-01');
    expect(decalerMois('2026-01', -1)).toBe('2025-12');
    expect(ajouterJours('2026-02-27', 3)).toBe('2026-03-02');
  });

  it('calcule le jour de la semaine (lundi = 0), le lundi et le nombre de jours', () => {
    expect(jourSemaine('2026-03-16')).toBe(0); // lundi
    expect(jourSemaine('2026-03-15')).toBe(6); // dimanche
    expect(lundiDe('2026-03-15')).toBe('2026-03-09');
    expect(lundiDe('2026-03-16')).toBe('2026-03-16');
    expect(joursDansLeMois('2028-02')).toBe(29);
  });
});
