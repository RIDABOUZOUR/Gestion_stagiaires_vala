import { Dashboard, DonneesTableau } from './dashboard';

describe('Dashboard (calculs)', () => {
  const vide: DonneesTableau = {
    resume: null, statuts: null, echeances: null, durees: null, profil: null, assiduite: null, retards: null,
    evaluations: null, candidatures: null, departements: null, filieres: null, organismes: null, encadrants: null,
  };

  it('affiche « — » pour une source indisponible et les valeurs sinon', () => {
    const sansDonnees = Dashboard.construireIndicateurs(vide);
    expect(sansDonnees.every((i) => i.valeur === '—')).toBeTrue();

    const avec = Dashboard.construireIndicateurs({
      ...vide,
      resume: { totalStagiaires: 12, stagiairesActifs: 7, totalStages: 6, stagesEnCours: 3, stagesPlanifies: 2, stagesAEcheance: 1,
        joursEcheance: 30, totalEncadrants: 4, totalDepartements: 3 },
      assiduite: { tauxGlobal: 92.5, parDepartement: {}, stagiaires: [] },
    });
    expect(avec[0]).toEqual(jasmine.objectContaining({ valeur: '7', detail: 'sur 12 au total' }));
    expect(avec.find((i) => i.libelle === 'Assiduité')?.valeur).toBe('92.5 %');
  });

  it('configure un anneau avec légende et un graphique en barres borné', () => {
    const anneau = Dashboard.configuration(
      { id: 'x', titre: 'X', icone: '', couleur: '', type: 'doughnut', donnees: () => null }, { 'En cours': 3, Terminé: 1 });
    expect(anneau.type).toBe('doughnut');
    expect(anneau.data.labels).toEqual(['En cours', 'Terminé']);

    const barres = Dashboard.configuration(
      { id: 'y', titre: 'Y', icone: '', couleur: '', type: 'bar', unite: '%', max: 100, donnees: () => null }, { RH: 80 });
    expect((barres.options as { scales: { y: { max: number } } }).scales.y.max).toBe(100);
  });
});
