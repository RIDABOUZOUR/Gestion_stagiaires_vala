import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { Departement } from '../services/departement';
import { Stage } from '../services/stage';
import { Stagiaire } from '../services/stagiaire';
import { DepartementModel, StageModel, StagiaireModel } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { extractApiError } from '../shared/api-error';

const PHONE_PATTERN = /^(\+\d{1,3}[-. ]?)?\d{10}$/;

@Component({
  selector: 'app-edit-stagiaire',
  standalone: false,
  templateUrl: './edit-stagiaire.html',
  styleUrls: ['./edit-stagiaire.css'],
})
export class EditStagiaire implements OnInit {
  form!: FormGroup;
  stagiaireId!: number;
  departementsList: DepartementModel[] = [];
  stagesList: StageModel[] = [];
  loading = false;
  saving = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly departementService: Departement,
    private readonly stageService: Stage,
    private readonly stagiaireService: Stagiaire,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.stagiaireId = Number(this.route.snapshot.paramMap.get('id'));

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

    this.loading = true;
    forkJoin({
      departements: this.departementService.getDepartements(),
      stages: this.stageService.getStages(),
      stagiaire: this.stagiaireService.getStagiaireById(this.stagiaireId),
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ departements, stages, stagiaire }) => {
          this.departementsList = departements;
          this.stagesList = stages;
          this.form.patchValue({
            nom: stagiaire.nom,
            prenom: stagiaire.prenom,
            email: stagiaire.email,
            telephone: stagiaire.telephone,
            dateNaissance: stagiaire.dateNaissance ? stagiaire.dateNaissance.substring(0, 10) : '',
            sexe: stagiaire.sexe,
            niveau: stagiaire.niveau,
            filiere: stagiaire.filiere,
            organisme: stagiaire.organisme,
            adresse: stagiaire.adresse,
            departement: stagiaire.departement?.id ?? '',
            stage: stagiaire.stage?.id ?? '',
          });
        },
        error: (err) => {
          this.notifications.error(extractApiError(err));
          this.router.navigate(['/stagiaires']);
        },
      });
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  handleUpdateStagiaire(): void {
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
      id: this.stagiaireId,
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
      .editStagiaire(this.stagiaireId, payload)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.notifications.success('Le stagiaire a été mis à jour avec succès.');
          this.router.navigate(['/stagiaires']);
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }
}
