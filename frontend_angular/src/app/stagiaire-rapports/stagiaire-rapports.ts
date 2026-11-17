import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { StageStatus, StagiaireModel } from '../models/stage.model';
import { Rapport, RapportRequest } from '../models/suivi.model';
import { SuiviService } from '../services/suivi';
import { extractApiError } from '../shared/api-error';
import { ajouterJours, aujourdhui, lundiDe } from '../shared/dates';
import { RAPPORT_STATUT } from '../shared/libelles';
import { NotificationService } from '../shared/notification/notification.service';

@Component({
  selector: 'app-stagiaire-rapports',
  standalone: false,
  templateUrl: './stagiaire-rapports.html',
})
export class StagiaireRapports implements OnInit {
  @Input({ required: true }) stagiaire!: StagiaireModel;

  readonly statuts = RAPPORT_STATUT;

  rapports: Rapport[] = [];
  loading = false;
  enregistrement = false;
  formulaireOuvert = false;
  /** Rapport en cours de modification (null = nouveau rapport). */
  enEdition: Rapport | null = null;
  form!: FormGroup;

  /** Examen d'un rapport par l'encadrant : id du rapport et commentaire saisi. */
  examen: { id: number; commentaire: string } | null = null;

  constructor(
    private readonly fb: FormBuilder,
    private readonly suivi: SuiviService,
    private readonly notifications: NotificationService,
  ) {}

  get stageAnnule(): boolean {
    return this.stagiaire.stage.status === StageStatus.ANNULE;
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      semaine: [aujourdhui(), [Validators.required]],
      travauxRealises: ['', [Validators.required, Validators.maxLength(4000)]],
      difficultes: ['', [Validators.maxLength(2000)]],
      objectifs: ['', [Validators.maxLength(2000)]],
    });
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.suivi
      .rapports(this.stagiaire.id)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (r) => (this.rapports = r),
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  finDeSemaine(lundi: string): string {
    return ajouterJours(lundi, 6);
  }

  modifiable(r: Rapport): boolean {
    return r.statut !== 'VALIDE';
  }

  nouveau(): void {
    this.enEdition = null;
    this.form.reset({ semaine: aujourdhui(), travauxRealises: '', difficultes: '', objectifs: '' });
    this.form.get('semaine')!.enable();
    this.formulaireOuvert = true;
  }

  modifier(r: Rapport): void {
    this.enEdition = r;
    this.form.reset({
      semaine: r.semaine,
      travauxRealises: r.travauxRealises,
      difficultes: r.difficultes ?? '',
      objectifs: r.objectifs ?? '',
    });
    this.form.get('semaine')!.disable(); // la semaine d'un rapport ne change pas
    this.formulaireOuvert = true;
  }

  annuler(): void {
    this.formulaireOuvert = false;
    this.enEdition = null;
  }

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  /** Semaine (du lundi au dimanche) correspondant à la date choisie. */
  get semaineChoisie(): string | null {
    const valeur = this.form.getRawValue().semaine as string;
    return valeur ? lundiDe(valeur) : null;
  }

  enregistrer(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const requete: RapportRequest = {
      semaine: v.semaine,
      travauxRealises: v.travauxRealises.trim(),
      difficultes: v.difficultes?.trim() || null,
      objectifs: v.objectifs?.trim() || null,
    };
    const appel = this.enEdition
      ? this.suivi.modifierRapport(this.enEdition.id, requete)
      : this.suivi.deposerRapport(this.stagiaire.id, requete);
    this.enregistrement = true;
    appel.pipe(finalize(() => (this.enregistrement = false))).subscribe({
      next: () => {
        this.notifications.success(this.enEdition ? 'Rapport modifié et soumis à nouveau.' : 'Rapport déposé.');
        this.annuler();
        this.charger();
      },
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }

  ouvrirExamen(r: Rapport): void {
    this.examen = { id: r.id, commentaire: '' };
  }

  decider(r: Rapport, decision: 'valider' | 'a-revoir'): void {
    const commentaire = this.examen?.id === r.id ? this.examen.commentaire.trim() : '';
    if (decision === 'a-revoir' && !commentaire) {
      this.notifications.error('Expliquez ce qu’il faut revoir dans le commentaire.');
      return;
    }
    const appel = decision === 'valider'
      ? this.suivi.validerRapport(r.id, commentaire || null)
      : this.suivi.demanderRevision(r.id, commentaire);
    appel.subscribe({
      next: (maj) => {
        this.rapports = this.rapports.map((x) => (x.id === maj.id ? maj : x));
        this.examen = null;
        this.notifications.success(decision === 'valider' ? 'Rapport validé.' : 'Révision demandée.');
      },
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }
}
