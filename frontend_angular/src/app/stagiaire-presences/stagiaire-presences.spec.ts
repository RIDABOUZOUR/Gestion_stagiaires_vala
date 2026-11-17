import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Sexe, StageStatus, StagiaireModel } from '../models/stage.model';
import { Presence } from '../models/suivi.model';
import { SuiviService } from '../services/suivi';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';
import { StagiairePresences } from './stagiaire-presences';

describe('StagiairePresences', () => {
  let fixture: ComponentFixture<StagiairePresences>;
  let component: StagiairePresences;
  let suivi: jasmine.SpyObj<SuiviService>;

  const stagiaire = (status: StageStatus = StageStatus.TERMINE): StagiaireModel => ({
    id: 5, nom: 'Alami', prenom: 'Sara', dateNaissance: '2003-05-14', sexe: Sexe.FEMME, email: 's@mail.com',
    telephone: '', adresse: '', organisme: 'ENSA', niveau: 'Bac+5', filiere: 'GI',
    departement: { id: 1, nom: 'Informatique', responsable: 'D' },
    stage: { id: 1, sujet: 'IA', dateDebut: '2025-03-04', dateFin: '2025-06-30', status, encadrant: { id: 1, nom: 'M', prenom: 'P', email: '', telephone: '' } },
  });

  const presence = (date: string, statut: Presence['statut']): Presence => ({ id: 1, stagiaireId: 5, date, statut, commentaire: null });

  async function creer(s: StagiaireModel, presences: Presence[] = []): Promise<void> {
    suivi = jasmine.createSpyObj<SuiviService>('SuiviService', ['presences', 'enregistrerPresence', 'supprimerPresence']);
    suivi.presences.and.returnValue(of(presences));
    await TestBed.configureTestingModule({
      declarations: [StagiairePresences],
      imports: TEST_IMPORTS,
      providers: [...TEST_PROVIDERS, { provide: SuiviService, useValue: suivi }],
    }).compileComponents();
    fixture = TestBed.createComponent(StagiairePresences);
    component = fixture.componentInstance;
    component.stagiaire = s;
    fixture.detectChanges();
  }

  it('affiche le dernier mois du stage quand le stage est passé, aligné sur le lundi', async () => {
    await creer(stagiaire());
    expect(component.mois).toBe('2025-06');
    expect(component.dernierMois).toBeTrue();
    // Le 1er juin 2025 est un dimanche : 6 cases vides avant.
    expect(component.cases.slice(0, 6).every((c) => c === null)).toBeTrue();
    expect(component.cases[6]?.date).toBe('2025-06-01');
    expect(component.cases[6]?.weekend).toBeTrue();
  });

  it('ne permet la saisie que pendant le stage', async () => {
    await creer(stagiaire());
    component.changerMois(-3); // mars 2025
    const jours = component.cases.filter((c) => c !== null);
    expect(jours.find((c) => c!.date === '2025-03-03')!.saisissable).toBeFalse(); // avant le début
    expect(jours.find((c) => c!.date === '2025-03-04')!.saisissable).toBeTrue();
    expect(component.premierMois).toBeTrue();
  });

  it("applique l'outil sélectionné et met à jour le décompte", async () => {
    await creer(stagiaire(), [presence('2025-06-02', 'PRESENT')]);
    suivi.enregistrerPresence.and.returnValue(of(presence('2025-06-03', 'RETARD')));
    component.outil = 'RETARD';

    component.appliquer(component.cases.find((c) => c?.date === '2025-06-03')!);

    expect(suivi.enregistrerPresence).toHaveBeenCalledWith(5, '2025-06-03', 'RETARD');
    expect(component.resume.find((r) => r.statut === 'RETARD')!.nombre).toBe(1);
    expect(component.tauxPresence).toBe(100);
  });

  it("efface une présence avec l'outil Effacer", async () => {
    await creer(stagiaire(), [presence('2025-06-02', 'ABSENT')]);
    suivi.supprimerPresence.and.returnValue(of(void 0));
    component.outil = 'EFFACER';

    component.appliquer(component.cases.find((c) => c?.date === '2025-06-02')!);

    expect(suivi.supprimerPresence).toHaveBeenCalledWith(5, '2025-06-02');
    expect(component.cases.find((c) => c?.date === '2025-06-02')!.presence).toBeUndefined();
  });

  it('bloque toute saisie pour un stage annulé', async () => {
    await creer(stagiaire(StageStatus.ANNULE));
    expect(component.cases.filter((c) => c?.saisissable).length).toBe(0);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('annulé');
  });
});
