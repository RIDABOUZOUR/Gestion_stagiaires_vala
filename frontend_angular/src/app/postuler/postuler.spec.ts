import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { Candidature } from '../models/candidature.model';
import { CandidatureService } from '../services/candidature';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';
import { Postuler } from './postuler';

describe('Postuler', () => {
  let fixture: ComponentFixture<Postuler>;
  let component: Postuler;
  let service: jasmine.SpyObj<CandidatureService>;

  beforeEach(async () => {
    service = jasmine.createSpyObj<CandidatureService>('CandidatureService', ['soumettre']);
    await TestBed.configureTestingModule({
      declarations: [Postuler],
      imports: TEST_IMPORTS,
      providers: [...TEST_PROVIDERS, { provide: CandidatureService, useValue: service }],
    }).compileComponents();
    fixture = TestBed.createComponent(Postuler);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const remplir = () => component.form.patchValue({ nom: 'Alami', prenom: 'Sara', email: 'sara@mail.com', sexe: 'FEMME' });

  it('refuse une période incohérente', () => {
    remplir();
    component.form.patchValue({ dateDebutSouhaitee: '2026-07-01', dateFinSouhaitee: '2026-04-01' });
    component.envoyer();
    expect(service.soumettre).not.toHaveBeenCalled();
    expect(component.form.errors?.['periode']).toBeTrue();
  });

  it('envoie les champs vides comme null puis confirme', () => {
    service.soumettre.and.returnValue(of({} as Candidature));
    remplir();
    component.envoyer();
    expect(service.soumettre).toHaveBeenCalledWith(jasmine.objectContaining({ nom: 'Alami', telephone: null, motivation: null, departementSouhaiteId: null }));
    expect(component.envoyee).toBeTrue();
  });

  it("affiche l'erreur de l'API (candidature déjà en cours)", () => {
    service.soumettre.and.returnValue(throwError(() => new HttpErrorResponse({ status: 409, error: { message: 'Une candidature est déjà en cours.' } })));
    remplir();
    component.envoyer();
    expect(component.erreur).toBe('Une candidature est déjà en cours.');
    expect(component.envoyee).toBeFalse();
  });
});
