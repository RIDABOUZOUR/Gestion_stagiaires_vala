import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Departement } from '../services/departement';
import { Stage } from '../services/stage';
import { Stagiaire } from '../services/stagiaire';

@Component({
  selector: 'app-edit-stagiaire',
  standalone: false,
  templateUrl: './edit-stagiaire.html',
  styleUrls: ['./edit-stagiaire.css']
})
export class EditStagiaire implements OnInit {
  editStagiaireFormGroup!: FormGroup;
  stagiaireId!: number;
  departementsList: any[] = [];
  stagesList: any[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private departementService: Departement,
    private stageService: Stage,
    private stagiaireService: Stagiaire
  ) {}

  ngOnInit(): void {
    this.stagiaireId = +this.route.snapshot.paramMap.get('id')!;

    this.editStagiaireFormGroup = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      filiere: ['', Validators.required],
      organisme: [''],
      departement: ['', Validators.required],
      stage: ['', Validators.required],
      sexe: ['', Validators.required],
      dateNaissance: ['', Validators.required]
    });

    forkJoin({
      departements: this.departementService.getDepartements(),
      stages: this.stageService.getStages(),
      stagiaire: this.stagiaireService.getStagiaireById(this.stagiaireId)
    }).subscribe({
      next: ({ departements, stages, stagiaire }) => {
        this.departementsList = departements;
        this.stagesList = stages;

        this.editStagiaireFormGroup.patchValue({
          nom: stagiaire.nom,
          prenom: stagiaire.prenom,
          email: stagiaire.email,
          filiere: stagiaire.filiere,
          organisme: stagiaire.organisme,
          departement: stagiaire.departement.id,
          stage: stagiaire.stage.id,
          sexe: stagiaire.sexe,
          dateNaissance: stagiaire.dateNaissance ? stagiaire.dateNaissance.substring(0, 10) : ''
        });
      },
      error: (err) => {
        console.error('Erreur chargement données', err);
        alert('Erreur lors du chargement des données du stagiaire');
        this.router.navigate(['/stagiaires']);
      }
    });
  }

  handleUpdateStagiaire() {
    if (this.editStagiaireFormGroup.invalid) {
      alert("Veuillez remplir tous les champs obligatoires");
      return;
    }

    const selectedDepartementId = this.editStagiaireFormGroup.value.departement;
    const selectedStageId = this.editStagiaireFormGroup.value.stage;

    const departementObject = this.departementsList.find(d => d.id == selectedDepartementId);
    const stageObject = this.stagesList.find(s => s.id == selectedStageId);

    if (!departementObject) {
      alert("Département introuvable");
      return;
    }

    if (!stageObject) {
      alert("Stage introuvable");
      return;
    }

    const updatedStagiaire = {
      ...this.editStagiaireFormGroup.value,
      departement: departementObject,
      stage: stageObject
    };

    this.stagiaireService.editStagiaire(this.stagiaireId, updatedStagiaire).subscribe({
      next: () => {
        alert("Le stagiaire a été mis à jour avec succès");
        this.router.navigate(['/stagiaires']);
      },
      error: (err) => {
        console.error(err);
        alert("Une erreur est survenue lors de la mise à jour");
      }
    });
  }
}
