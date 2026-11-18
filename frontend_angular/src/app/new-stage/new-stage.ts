import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { Encadrant } from '../services/encadrant';
import { Stage } from '../services/stage';
import { EncadrantModel, StageModel, StageStatus } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { extractApiError } from '../shared/api-error';
import { periodeCoherente } from '../shared/validateurs';

@Component({
  selector: 'app-new-stage',
  standalone: false,
  templateUrl: './new-stage.html',
  styleUrl: './new-stage.css',
})
export class NewStage implements OnInit {
  encadrantsList: EncadrantModel[] = [];
  form!: FormGroup;
  saving = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly encadrantService: Encadrant,
    private readonly stageService: Stage,
    private readonly router: Router,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      sujet: ['', [Validators.required, Validators.maxLength(100)]],
      dateDebut: ['', [Validators.required]],
      dateFin: ['', [Validators.required]],
      status: [StageStatus.PLANIFIE, [Validators.required]],
      encadrant: ['', [Validators.required]],
    }, { validators: periodeCoherente('dateDebut', 'dateFin') });

    this.loadEncadrants();
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  private loadEncadrants(): void {
    this.encadrantService.getEncadrants().subscribe({
      next: (data) => (this.encadrantsList = data),
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }

  handleSaveStage(): void {
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
      id: 0,
      sujet: value.sujet,
      dateDebut: value.dateDebut,
      dateFin: value.dateFin,
      status: value.status,
      encadrant,
    };

    this.saving = true;
    this.stageService
      .addStage(payload)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.notifications.success('Le stage a été enregistré avec succès.');
          this.router.navigate(['/stages']);
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }
}
