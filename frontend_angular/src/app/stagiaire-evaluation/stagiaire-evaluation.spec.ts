import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { StageStatus, StagiaireModel } from '../models/stage.model';
import { EvaluationService } from '../services/evaluation';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';
import { StagiaireEvaluation } from './stagiaire-evaluation';

describe('StagiaireEvaluation', () => {
  let fixture: ComponentFixture<StagiaireEvaluation>;
  let component: StagiaireEvaluation;
  let service: jasmine.SpyObj<EvaluationService>;

  async function creer(status: StageStatus): Promise<void> {
    service = jasmine.createSpyObj<EvaluationService>('EvaluationService', ['evaluation', 'enregistrer']);
    service.evaluation.and.returnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
    await TestBed.configureTestingModule({
      declarations: [StagiaireEvaluation],
      imports: TEST_IMPORTS,
      providers: [...TEST_PROVIDERS, { provide: EvaluationService, useValue: service }],
    }).compileComponents();
    fixture = TestBed.createComponent(StagiaireEvaluation);
    component = fixture.componentInstance;
    component.stagiaire = { id: 5, stage: { status } } as StagiaireModel;
    fixture.detectChanges();
  }

  it("n'est pas disponible tant que le stage n'est pas terminé", async () => {
    await creer(StageStatus.EN_COURS);
    expect(component.disponible).toBeFalse();
    expect(service.evaluation).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('une fois le stage terminé');
  });

  it('enregistre la note (virgule acceptée) et les compétences', async () => {
    await creer(StageStatus.TERMINE);
    service.enregistrer.and.returnValue(of({ id: 1, stagiaireId: 5, noteGlobale: 15.5, competenceTechnique: 4, communication: 5,
      autonomie: 3, assiduite: 4, appreciation: null, recommande: true, dateEvaluation: '2026-07-02' }));
    component.form.patchValue({ noteGlobale: '15,5', competenceTechnique: 4, communication: 5, autonomie: 3, assiduite: 4, recommande: true });

    component.enregistrer();

    expect(service.enregistrer).toHaveBeenCalledWith(5, jasmine.objectContaining({ noteGlobale: 15.5, communication: 5, recommande: true, appreciation: null }));
    expect(component.evaluation?.id).toBe(1);
  });

  it('refuse une note hors de 0 à 20', async () => {
    await creer(StageStatus.TERMINE);
    component.form.patchValue({ noteGlobale: 21 });
    component.enregistrer();
    expect(service.enregistrer).not.toHaveBeenCalled();
  });
});
