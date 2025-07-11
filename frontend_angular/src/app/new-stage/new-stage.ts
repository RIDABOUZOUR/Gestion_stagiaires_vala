import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Encadrant } from '../services/encadrant';
import { Stage } from '../services/stage';


@Component({
  selector: 'app-new-stage',
  standalone: false,
  templateUrl: './new-stage.html',
  styleUrl: './new-stage.css'
})
export class NewStage implements OnInit{
  encadrantsList: any[] = [];
  newStageFormGroup! : FormGroup;

  constructor(private fb:FormBuilder,private encadrantService:Encadrant ,private stageService:Stage){}
  ngOnInit(): void {
    this.newStageFormGroup=this.fb.group({
      sujet: ['', Validators.required],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      encadrant: ['', Validators.required],
      status: ['EN_COURS']
    });
     this.encadrantService.getEncadrants().subscribe(
      (data) => {
        this.encadrantsList = data;
      },
      (error) => {
        console.error("Erreur lors du chargement des encadrants", error);
      }
    );
  }
  handleSaveStage() {
  if (this.newStageFormGroup.invalid) {
    alert("Veuillez remplir tous les champs obligatoires");
    return;
  }
  
  const selectedEncadrantId = this.newStageFormGroup.value.encadrant;
  const encadrantObject = this.encadrantsList.find(e => e.id == selectedEncadrantId);

  if (!encadrantObject) {
    alert("Encadrant introuvable");
    return;
  }

  // Créer un objet stage avec encadrant complet
  const stage = {
    ...this.newStageFormGroup.value,
    encadrant: encadrantObject
  };

  this.stageService.addStage(stage).subscribe({
    next: () => {
      alert("Le stage a été enregistré avec succès");
      this.newStageFormGroup.reset();
    },
    error: (err) => {
      console.error(err);
      alert("Une erreur est survenue lors de l'enregistrement");
    }
  });
}
}
