import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { Candidature } from '../models/candidature.model';
import { AuthService } from '../services/auth';
import { CandidatureService } from '../services/candidature';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';
import { Postuler } from './postuler';

describe('Postuler', () => {
  let fixture: ComponentFixture<Postuler>;
  let component: Postuler;
  let service: jasmine.SpyObj<CandidatureService>;
  let connecte: boolean;

  const pdf = () => new File(['%PDF-1.7 contenu'], 'cv.pdf', { type: 'application/pdf' });
  const choisir = (fichier: File) =>
    component.choisirCv({ target: { files: [fichier], value: 'x' } } as unknown as Event);

  beforeEach(async () => {
    connecte = false;
    service = jasmine.createSpyObj<CandidatureService>('CandidatureService', ['soumettre', 'departements']);
    service.departements.and.returnValue(of([{ id: 2, nom: 'Informatique' }]));
    await TestBed.configureTestingModule({
      declarations: [Postuler],
      imports: TEST_IMPORTS,
      providers: [
        ...TEST_PROVIDERS,
        { provide: CandidatureService, useValue: service },
        { provide: AuthService, useValue: { estConnecte: () => connecte } },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(Postuler);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const remplir = () => component.form.patchValue({ nom: 'Alami', prenom: 'Sara', email: 'sara@mail.com', sexe: 'FEMME' });

  it('propose les départements avec « Sans préférence »', () => {
    const options = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('#departementSouhaiteId option'))
      .map((o) => o.textContent?.trim());
    expect(options).toEqual(['Sans préférence', 'Informatique']);
  });

  it("exige un CV pour un visiteur", () => {
    remplir();
    component.envoyer();
    expect(service.soumettre).not.toHaveBeenCalled();
    expect(component.erreurFichier).toBe('Joignez votre CV au format PDF.');
  });

  it('refuse un fichier qui n’est pas un PDF ou qui dépasse 5 Mo', () => {
    choisir(new File(['x'], 'cv.docx', { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' }));
    expect(component.erreurFichier).toBe('Le CV doit être un fichier PDF.');
    expect(component.cv).toBeNull();

    const lourd = new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'cv.pdf', { type: 'application/pdf' });
    choisir(lourd);
    expect(component.erreurFichier).toBe('Le CV dépasse 5 Mo.');
  });

  it('envoie la candidature, le département choisi et le CV', () => {
    service.soumettre.and.returnValue(of({} as Candidature));
    remplir();
    component.form.patchValue({ departementSouhaiteId: 2 });
    const fichier = pdf();
    choisir(fichier);

    component.envoyer();

    expect(service.soumettre).toHaveBeenCalledWith(
      jasmine.objectContaining({ nom: 'Alami', departementSouhaiteId: 2, telephone: null }), fichier);
    expect(component.envoyee).toBeTrue();
  });

  it('un RH connecté peut saisir une candidature sans CV', () => {
    connecte = true;
    service.soumettre.and.returnValue(of({} as Candidature));
    remplir();

    component.envoyer();

    expect(component.cvObligatoire).toBeFalse();
    expect(service.soumettre).toHaveBeenCalledWith(jasmine.objectContaining({ departementSouhaiteId: null }), null);
  });

  it('refuse une période incohérente', () => {
    remplir();
    choisir(pdf());
    component.form.patchValue({ dateDebutSouhaitee: '2026-07-01', dateFinSouhaitee: '2026-04-01' });
    component.envoyer();
    expect(service.soumettre).not.toHaveBeenCalled();
    expect(component.form.errors?.['periode']).toBeTrue();
  });

  it("affiche l'erreur de l'API", () => {
    service.soumettre.and.returnValue(throwError(() => new HttpErrorResponse({ status: 400, error: { message: 'Le CV doit être un fichier PDF.' } })));
    remplir();
    choisir(pdf());
    component.envoyer();
    expect(component.erreur).toBe('Le CV doit être un fichier PDF.');
    expect(component.envoyee).toBeFalse();
  });
});
