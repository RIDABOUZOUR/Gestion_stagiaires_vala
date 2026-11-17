import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { Candidature } from '../models/candidature.model';
import { StageStatus } from '../models/stage.model';
import { CandidatureService } from '../services/candidature';
import { Stage } from '../services/stage';
import { Departement } from '../services/departement';
import { NotificationService } from '../shared/notification/notification.service';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';
import { CandidatureDetail } from './candidature-detail';

describe('CandidatureDetail', () => {
  let fixture: ComponentFixture<CandidatureDetail>;
  let component: CandidatureDetail;
  let service: jasmine.SpyObj<CandidatureService>;
  let notifications: NotificationService;

  const candidature = (statut: Candidature['statut']): Candidature => ({
    id: 1, nom: 'Alami', prenom: 'Sara', email: 's@mail.com', telephone: null, dateNaissance: null, sexe: null, organisme: 'ENSA',
    niveau: null, filiere: null, departementSouhaiteId: 2, departementSouhaiteNom: 'Informatique', dateDebutSouhaitee: null,
    dateFinSouhaitee: null, motivation: null, statut, motifRefus: null, dateSoumission: '2026-03-01T10:00:00Z', dateDecision: null, stagiaireId: null,
    cvFourni: true,
  });

  beforeEach(async () => {
    service = jasmine.createSpyObj<CandidatureService>('CandidatureService', ['candidature', 'etudier', 'refuser', 'retirer', 'accepter', 'supprimer', 'cv', 'remplacerCv']);
    service.candidature.and.returnValue(of(candidature('EN_ETUDE')));
    const stages = jasmine.createSpyObj<Stage>('Stage', ['getStages']);
    stages.getStages.and.returnValue(of([
      { id: 7, sujet: 'IA', dateDebut: '2026-04-01', dateFin: '2026-07-31', status: StageStatus.PLANIFIE, encadrant: {} as never },
      { id: 8, sujet: 'Ancien', dateDebut: '2025-01-01', dateFin: '2025-03-31', status: StageStatus.TERMINE, encadrant: {} as never },
    ]));
    const departements = jasmine.createSpyObj<Departement>('Departement', ['getDepartements']);
    departements.getDepartements.and.returnValue(of([]));
    await TestBed.configureTestingModule({
      declarations: [CandidatureDetail],
      imports: TEST_IMPORTS,
      providers: [
        ...TEST_PROVIDERS,
        { provide: CandidatureService, useValue: service },
        { provide: Stage, useValue: stages },
        { provide: Departement, useValue: departements },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }) } } },
      ],
    }).compileComponents();
    notifications = TestBed.inject(NotificationService);
    fixture = TestBed.createComponent(CandidatureDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("ne propose que les stages ouverts et le département souhaité par défaut", () => {
    component.ouvrirAcceptation();
    expect(component.stagesOuverts.map((s) => s.id)).toEqual([7]);
    expect(component.departementId).toBe(2);
  });

  it('accepte avec le stage choisi', () => {
    service.accepter.and.returnValue(of({ ...candidature('ACCEPTEE'), stagiaireId: 40 }));
    component.ouvrirAcceptation();
    component.stageId = 7;

    component.accepter();

    expect(service.accepter).toHaveBeenCalledWith(1, 7, 2);
    expect(component.candidature?.statut).toBe('ACCEPTEE');
    expect(component.panneau).toBeNull();
  });

  it('exige un motif pour refuser', () => {
    spyOn(notifications, 'error');
    component.ouvrirRefus();
    component.refuser();
    expect(service.refuser).not.toHaveBeenCalled();
    expect(notifications.error).toHaveBeenCalled();
  });

  it('affiche le bouton « Voir le CV » quand un CV est joint', () => {
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Voir le CV');
  });

  it('refuse un fichier qui n’est pas un PDF sans appeler l’API', () => {
    spyOn(notifications, 'error');
    const champ = { files: [new File(['texte'], 'cv.txt', { type: 'text/plain' })], value: 'x' } as unknown as HTMLInputElement;

    component.envoyerCv({ target: champ } as unknown as Event);

    expect(notifications.error).toHaveBeenCalledWith('Le CV doit être un fichier PDF.');
    expect(service.remplacerCv).not.toHaveBeenCalled();
  });
});
