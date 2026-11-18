import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { Departement } from '../services/departement';
import { Stage } from '../services/stage';
import { Stagiaire } from '../services/stagiaire';
import { DepartementModel, StageModel, StagiaireModel } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { extractApiError } from '../shared/api-error';

const PHONE_PATTERN = /^(\+\d{1,3}[-. ]?)?\d{10}$/;

@Component({
  selector: 'app-new-stagiaire',
  standalone: false,
  templateUrl: './new-stagiaire.html',
  styleUrl: './new-stagiaire.css',
})
export class NewStagiaire implements OnInit {
  departementsList: DepartementModel[] = [];
  stagesList: StageModel[] = [];
  form!: FormGroup;
  saving = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly departementService: Departement,
    private readonly stageService: Stage,
    private readonly stagiaireService: Stagiaire,
    private readonly router: Router,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
      prenom: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.pattern(PHONE_PATTERN)]],
      dateNaissance: ['', [Validators.required]],
      sexe: ['', [Validators.required]],
      niveau: [''],
      filiere: ['', [Validators.required]],
      organisme: [''],
      adresse: ['', [Validators.maxLength(200)]],
      departement: ['', [Validators.required]],
      stage: ['', [Validators.required]],
    });

    this.loadDepartements();
    this.loadStages();
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  private loadDepartements(): void {
    this.departementService.getDepartements().subscribe({
      next: (data) => (this.departementsList = data),
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }

  private loadStages(): void {
    this.stageService.getStages().subscribe({
      next: (data) => (this.stagesList = data),
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }

  handleSaveStagiaire(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notifications.error('Veuillez corriger les champs en surbrillance.');
      return;
    }

    const value = this.form.value;
    const departement = this.departementsList.find((d) => d.id == value.departement);
    const stage = this.stagesList.find((s) => s.id == value.stage);

    if (!departement || !stage) {
      this.notifications.error('Département ou stage introuvable.');
      return;
    }

    const payload: StagiaireModel = {
      id: 0,
      nom: value.nom,
      prenom: value.prenom,
      email: value.email,
      telephone: value.telephone,
      dateNaissance: value.dateNaissance,
      sexe: value.sexe,
      niveau: value.niveau,
      filiere: value.filiere,
      organisme: value.organisme,
      adresse: value.adresse,
      departement,
      stage,
    };

    this.saving = true;
    this.stagiaireService
      .addStagiaire(payload)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.notifications.success('Le stagiaire a été enregistré avec succès.');
          this.router.navigate(['/stagiaires']);
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }
}
