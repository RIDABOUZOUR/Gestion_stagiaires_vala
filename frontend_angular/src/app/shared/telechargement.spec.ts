import { nomDuFichier, parametres } from './telechargement';

describe('telechargement', () => {
  it('extrait le nom de fichier de Content-Disposition', () => {
    expect(nomDuFichier('attachment; filename="stagiaires-2026-03-15.csv"')).toBe('stagiaires-2026-03-15.csv');
    expect(nomDuFichier("attachment; filename*=UTF-8''attestation-%C3%A9l%C3%A8ve.pdf")).toBe('attestation-élève.pdf');
    expect(nomDuFichier(null)).toBeNull();
  });

  it('retire les paramètres vides', () => {
    expect(parametres({ keyword: ' ali ', statut: '', departementId: null, page: 0 })).toEqual({ keyword: 'ali', page: '0' });
  });
});
