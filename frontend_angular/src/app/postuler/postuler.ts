import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { DepartementPublic } from '../models/candidature.model';
import { Sexe } from '../models/stage.model';
import { AuthService } from '../services/auth';
import { CandidatureService } from '../services/candidature';
import { extractApiError } from '../shared/api-error';
import { erreurCv, tailleLisible } from '../shared/cv';
import { periodeCoherente } from '../shared/validateurs';

const PHONE_PATTERN = /^(\+\d{1,3}[-. ]?)?\d{10}$/;

/**
 * Dépôt d'une candidature. Page publique : accessible sans compte, avec le CV
 * (PDF, obligatoire pour un visiteur) et le département souhaité (facultatif).
 */
@Component({
  selector: 'app-postuler',
  standalone: false,
  templateUrl: './postuler.html',
})
export class Postuler implements OnInit {
  readonly sexes = [Sexe.FEMME, Sexe.HOMME];

  form!: FormGroup;
  departements: DepartementPublic[] = [];
  /** CV choisi et éventuelle erreur (type, taille, absence). */
  cv: File | null = null;
  erreurFichier: string | null = null;
  envoi = false;
  erreur: string | null = null;
  envoyee = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly candidatureService: CandidatureService,
    readonly auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group(
      {
        nom: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
        prenom: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
        email: ['', [Validators.required, Validators.email]],
        telephone: ['', [Validators.pattern(PHONE_PATTERN)]],
        dateNaissance: [''],
        sexe: ['', [Validators.required]],
        organisme: ['', [Validators.maxLength(100)]],
        niveau: ['', [Validators.maxLength(50)]],
        filiere: ['', [Validators.maxLength(100)]],
        departementSouhaiteId: [null as number | null],
        dateDebutSouhaitee: [''],
        dateFinSouhaitee: [''],
        motivation: ['', [Validators.maxLength(4000)]],
      },
      { validators: periodeCoherente('dateDebutSouhaitee', 'dateFinSouhaitee') },
    );
    // Liste facultative : en cas d'échec, le candidat garde « Sans préférence ».
    this.candidatureService.departements().subscribe({ next: (d) => (this.departements = d), error: () => undefined });
  }

  /** Le CV est obligatoire pour un visiteur ; un RH connecté peut saisir une candidature sans CV. */
  get cvObligatoire(): boolean {
    return !this.auth.estConnecte();
  }

  get tailleCv(): string {
    return this.cv ? tailleLisible(this.cv.size) : '';
  }

  choisirCv(evenement: Event): void {
    const champ = evenement.target as HTMLInputElement;
    const fichier = champ.files?.[0] ?? null;
    this.erreurFichier = erreurCv(fichier);
    this.cv = this.erreurFichier ? null : fichier;
    if (this.erreurFichier) {
      champ.value = '';
    }
  }

  retirerCv(champ: HTMLInputElement): void {
    this.cv = null;
    this.erreurFichier = null;
    champ.value = '';
  }

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  envoyer(): void {
    if (!this.cv && this.cvObligatoire) {
      this.erreurFichier = 'Joignez votre CV au format PDF.';
    }
    if (this.form.invalid || this.erreurFichier) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.value;
    const vide = (x: string) => (x && String(x).trim() ? String(x).trim() : null);
    this.erreur = null;
    this.envoi = true;
    this.candidatureService
      .soumettre({
        nom: v.nom.trim(),
        prenom: v.prenom.trim(),
        email: v.email.trim(),
        telephone: vide(v.telephone),
        dateNaissance: vide(v.dateNaissance),
        sexe: v.sexe,
        organisme: vide(v.organisme),
        niveau: vide(v.niveau),
        filiere: vide(v.filiere),
        departementSouhaiteId: v.departementSouhaiteId ? Number(v.departementSouhaiteId) : null,
        dateDebutSouhaitee: vide(v.dateDebutSouhaitee),
        dateFinSouhaitee: vide(v.dateFinSouhaitee),
        motivation: vide(v.motivation),
      }, this.cv)
      .pipe(finalize(() => (this.envoi = false)))
      .subscribe({
        next: () => (this.envoyee = true),
        error: (err) => (this.erreur = extractApiError(err)),
      });
  }

  recommencer(): void {
    this.form.reset({ sexe: '', departementSouhaiteId: null });
    this.cv = null;
    this.erreurFichier = null;
    this.envoyee = false;
  }
}
