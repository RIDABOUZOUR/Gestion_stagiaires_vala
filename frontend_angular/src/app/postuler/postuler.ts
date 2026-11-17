import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Sexe } from '../models/stage.model';
import { AuthService } from '../services/auth';
import { CandidatureService } from '../services/candidature';
import { extractApiError } from '../shared/api-error';

const PHONE_PATTERN = /^(\+\d{1,3}[-. ]?)?\d{10}$/;

/** Période souhaitée cohérente : début avant fin (si les deux sont renseignées). */
function periodeCoherente(group: AbstractControl): ValidationErrors | null {
  const debut = group.get('dateDebutSouhaitee')?.value;
  const fin = group.get('dateFinSouhaitee')?.value;
  return debut && fin && debut >= fin ? { periode: true } : null;
}

/**
 * Dépôt d'une candidature. Page publique : accessible sans compte (le
 * département souhaité est alors saisi librement, la liste n'étant pas publique).
 */
@Component({
  selector: 'app-postuler',
  standalone: false,
  templateUrl: './postuler.html',
})
export class Postuler implements OnInit {
  readonly sexes = [Sexe.FEMME, Sexe.HOMME];

  form!: FormGroup;
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
        dateDebutSouhaitee: [''],
        dateFinSouhaitee: [''],
        motivation: ['', [Validators.maxLength(4000)]],
      },
      { validators: periodeCoherente },
    );
  }

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  envoyer(): void {
    if (this.form.invalid) {
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
        departementSouhaiteId: null,
        dateDebutSouhaitee: vide(v.dateDebutSouhaitee),
        dateFinSouhaitee: vide(v.dateFinSouhaitee),
        motivation: vide(v.motivation),
      })
      .pipe(finalize(() => (this.envoi = false)))
      .subscribe({
        next: () => (this.envoyee = true),
        error: (err) => (this.erreur = extractApiError(err)),
      });
  }

  recommencer(): void {
    this.form.reset({ sexe: '' });
    this.envoyee = false;
  }
}
