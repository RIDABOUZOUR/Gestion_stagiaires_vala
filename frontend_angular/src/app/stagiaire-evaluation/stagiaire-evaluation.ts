import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { StageStatus, StagiaireModel } from '../models/stage.model';
import { Evaluation } from '../models/suivi.model';
import { EvaluationService } from '../services/evaluation';
import { extractApiError } from '../shared/api-error';
import { NotificationService } from '../shared/notification/notification.service';

@Component({
  selector: 'app-stagiaire-evaluation',
  standalone: false,
  templateUrl: './stagiaire-evaluation.html',
})
export class StagiaireEvaluation implements OnInit {
  @Input({ required: true }) stagiaire!: StagiaireModel;

  readonly competences = [
    { champ: 'competenceTechnique', libelle: 'Compétence technique' },
    { champ: 'communication', libelle: 'Communication' },
    { champ: 'autonomie', libelle: 'Autonomie' },
    { champ: 'assiduite', libelle: 'Assiduité' },
  ];
  readonly niveaux = [1, 2, 3, 4, 5];

  form!: FormGroup;
  evaluation: Evaluation | null = null;
  loading = false;
  enregistrement = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly evaluationService: EvaluationService,
    private readonly notifications: NotificationService,
  ) {}

  /** L'évaluation n'est possible qu'une fois le stage terminé. */
  get disponible(): boolean {
    return this.stagiaire.stage.status === StageStatus.TERMINE;
  }

  ngOnInit(): void {
    const competence = [3, [Validators.required, Validators.min(1), Validators.max(5)]];
    this.form = this.fb.group({
      noteGlobale: [null, [Validators.required, Validators.min(0), Validators.max(20), Validators.pattern(/^\d{1,2}([.,]\d)?$/)]],
      competenceTechnique: competence,
      communication: competence,
      autonomie: competence,
      assiduite: competence,
      appreciation: ['', [Validators.maxLength(2000)]],
      recommande: [false],
    });
    if (this.disponible) {
      this.charger();
    }
  }

  charger(): void {
    this.loading = true;
    this.evaluationService
      .evaluation(this.stagiaire.id)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (e) => {
          this.evaluation = e;
          this.form.patchValue({ ...e, appreciation: e.appreciation ?? '' });
        },
        error: (err: unknown) => {
          // 404 : pas encore d'évaluation, le formulaire reste vierge.
          if (!(err instanceof HttpErrorResponse && err.status === 404)) {
            this.notifications.error(extractApiError(err));
          }
        },
      });
  }

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  enregistrer(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.value;
    this.enregistrement = true;
    this.evaluationService
      .enregistrer(this.stagiaire.id, {
        noteGlobale: Number(String(v.noteGlobale).replace(',', '.')),
        competenceTechnique: Number(v.competenceTechnique),
        communication: Number(v.communication),
        autonomie: Number(v.autonomie),
        assiduite: Number(v.assiduite),
        appreciation: v.appreciation?.trim() || null,
        recommande: !!v.recommande,
      })
      .pipe(finalize(() => (this.enregistrement = false)))
      .subscribe({
        next: (e) => {
          this.evaluation = e;
          this.notifications.success('Évaluation enregistrée.');
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }
}
