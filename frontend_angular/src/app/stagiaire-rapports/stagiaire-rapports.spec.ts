import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Sexe, StageStatus, StagiaireModel } from '../models/stage.model';
import { Rapport } from '../models/suivi.model';
import { SuiviService } from '../services/suivi';
import { NotificationService } from '../shared/notification/notification.service';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';
import { StagiaireRapports } from './stagiaire-rapports';

describe('StagiaireRapports', () => {
  let fixture: ComponentFixture<StagiaireRapports>;
  let component: StagiaireRapports;
  let suivi: jasmine.SpyObj<SuiviService>;
  let notifications: NotificationService;

  const rapport = (statut: Rapport['statut']): Rapport => ({
    id: 7, stagiaireId: 5, semaine: '2026-03-09', travauxRealises: 'API', difficultes: null, objectifs: null,
    dateDepot: '2026-03-13T10:00:00Z', statut, commentaireEncadrant: null,
  });

  beforeEach(async () => {
    suivi = jasmine.createSpyObj<SuiviService>('SuiviService', ['rapports', 'deposerRapport', 'modifierRapport', 'validerRapport', 'demanderRevision']);
    suivi.rapports.and.returnValue(of([rapport('SOUMIS')]));
    await TestBed.configureTestingModule({
      declarations: [StagiaireRapports],
      imports: TEST_IMPORTS,
      providers: [...TEST_PROVIDERS, { provide: SuiviService, useValue: suivi }],
    }).compileComponents();
    notifications = TestBed.inject(NotificationService);
    fixture = TestBed.createComponent(StagiaireRapports);
    component = fixture.componentInstance;
    component.stagiaire = {
      id: 5, nom: 'A', prenom: 'S', dateNaissance: '', sexe: Sexe.FEMME, email: '', telephone: '', adresse: '', organisme: '',
      niveau: '', filiere: '', departement: { id: 1, nom: 'I', responsable: '' },
      stage: { id: 1, sujet: 'IA', dateDebut: '2026-03-02', dateFin: '2026-06-30', status: StageStatus.EN_COURS, encadrant: { id: 1, nom: '', prenom: '', email: '', telephone: '' } },
    } as StagiaireModel;
    fixture.detectChanges();
  });

  it('affiche les rapports et la semaine du lundi au dimanche', () => {
    expect(component.rapports.length).toBe(1);
    expect(component.finDeSemaine('2026-03-09')).toBe('2026-03-15');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Semaine du 09/03/2026 au 15/03/2026');
  });

  it('dépose un nouveau rapport avec les champs nettoyés', () => {
    suivi.deposerRapport.and.returnValue(of(rapport('SOUMIS')));
    component.nouveau();
    component.form.patchValue({ semaine: '2026-03-12', travauxRealises: '  Tests  ', difficultes: ' ', objectifs: 'Déploiement' });

    component.enregistrer();

    expect(suivi.deposerRapport).toHaveBeenCalledWith(5, { semaine: '2026-03-12', travauxRealises: 'Tests', difficultes: null, objectifs: 'Déploiement' });
    expect(component.formulaireOuvert).toBeFalse();
  });

  it('exige un commentaire pour demander une révision', () => {
    spyOn(notifications, 'error');
    component.ouvrirExamen(component.rapports[0]);

    component.decider(component.rapports[0], 'a-revoir');

    expect(suivi.demanderRevision).not.toHaveBeenCalled();
    expect(notifications.error).toHaveBeenCalled();
  });

  it('valide un rapport et remplace la ligne', () => {
    suivi.validerRapport.and.returnValue(of(rapport('VALIDE')));
    component.ouvrirExamen(component.rapports[0]);
    component.examen!.commentaire = 'Bien';

    component.decider(component.rapports[0], 'valider');

    expect(suivi.validerRapport).toHaveBeenCalledWith(7, 'Bien');
    expect(component.rapports[0].statut).toBe('VALIDE');
    expect(component.modifiable(component.rapports[0])).toBeFalse();
  });
});
