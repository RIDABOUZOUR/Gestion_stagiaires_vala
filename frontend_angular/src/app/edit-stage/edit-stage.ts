import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { Encadrant } from '../services/encadrant';
import { Stage } from '../services/stage';
import { EncadrantModel, StageModel, StageStatus } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { extractApiError } from '../shared/api-error';

@Component({
  selector: 'app-edit-stage',
  standalone: false,
  templateUrl: './edit-stage.html',
  styleUrls: ['./edit-stage.css'],
})
export class EditStage implements OnInit {
  form!: FormGroup;
  stageId!: number;
  encadrantsList: EncadrantModel[] = [];
  loading = false;
  saving = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly stageService: Stage,
    private readonly encadrantService: Encadrant,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.stageId = Number(this.route.snapshot.paramMap.get('id'));

    this.form = this.fb.group({
      sujet: ['', [Validators.required, Validators.maxLength(100)]],
      dateDebut: ['', [Validators.required]],
      dateFin: ['', [Validators.required]],
      status: [StageStatus.PLANIFIE, [Validators.required]],
      encadrant: ['', [Validators.required]],
    });

    this.loading = true;
    forkJoin({
      encadrants: this.encadrantService.getEncadrants(),
      stage: this.stageService.getStageById(this.stageId),
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ encadrants, stage }) => {
          this.encadrantsList = encadrants;
          this.form.patchValue({
            sujet: stage.sujet,
            dateDebut: stage.dateDebut ? stage.dateDebut.substring(0, 10) : '',
            dateFin: stage.dateFin ? stage.dateFin.substring(0, 10) : '',
            status: stage.status ?? StageStatus.PLANIFIE,
            encadrant: stage.encadrant?.id ?? '',
          });
        },
        error: (err) => {
          this.notifications.error(extractApiError(err));
          this.router.navigate(['/stages']);
        },
      });
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  handleUpdateStage(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notifications.error('Veuillez corriger les champs en surbrillance.');
      return;
    }

    const value = this.form.value;
    const encadrant = this.encadrantsList.find((e) => e.id == value.encadrant);

    if (!encadrant) {
      this.notifications.error('Encadrant introuvable.');
      return;
    }

    const payload: StageModel = {
      id: this.stageId,
      sujet: value.sujet,
      dateDebut: value.dateDebut,
      dateFin: value.dateFin,
      status: value.status,
      encadrant,
    };

    this.saving = true;
    this.stageService
      .editStage(this.stageId, payload)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.notifications.success('Le stage a été mis à jour avec succès.');
          this.router.navigate(['/stages']);
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }
}
