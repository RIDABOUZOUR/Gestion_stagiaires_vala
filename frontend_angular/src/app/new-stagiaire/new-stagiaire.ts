import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Departement } from '../services/departement';
import { Stage } from '../services/stage';
import { Stagiaire } from '../services/stagiaire';

@Component({
  selector: 'app-new-stagiaire',
  standalone: false,
  templateUrl: './new-stagiaire.html',
  styleUrl: './new-stagiaire.css'
})
export class NewStagiaire implements OnInit {
  departementsList: any[] = [];
  stagesList: any[] = [];
  newStagiaireFormGroup!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private departementService: Departement,
    private stageService: Stage,
    private stagiaireService: Stagiaire
  ) {}

  ngOnInit(): void {
    this.newStagiaireFormGroup = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      filiere: ['', Validators.required],
      organisme: [''],
      departement: ['', Validators.required],
      stage: ['', Validators.required],
      sexe: ['', Validators.required],
      dateNaissance:['',Validators.required]
    });

    this.loadDepartements();
    this.loadStages();
  }

  loadDepartements() {
    this.departementService.getDepartements().subscribe(
      (data) => {
        this.departementsList = data;
      },
      (error) => {
        console.error("Erreur lors du chargement des départements", error);
      }
    );
  }

  loadStages() {
    this.stageService.getStages().subscribe(
      (data) => {
        this.stagesList = data;
      },
      (error) => {
        console.error("Erreur lors du chargement des stages", error);
      }
    );
  }

  handleSaveStagiaire() {
    if (this.newStagiaireFormGroup.invalid) {
      alert("Veuillez remplir tous les champs obligatoires");
      return;
    }
    
    const selectedDepartementId = this.newStagiaireFormGroup.value.departement;
    const selectedStageId = this.newStagiaireFormGroup.value.stage;

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

    const stagiaire = {
      ...this.newStagiaireFormGroup.value,
      departement: departementObject,
      stage: stageObject
    };

    this.stagiaireService.addStagiaire(stagiaire).subscribe({
      next: () => {
        alert("Le stagiaire a été enregistré avec succès");
        this.newStagiaireFormGroup.reset();
      },
      error: (err) => {
        console.error(err);
        alert("Une erreur est survenue lors de l'enregistrement");
      }
    });
  }
}