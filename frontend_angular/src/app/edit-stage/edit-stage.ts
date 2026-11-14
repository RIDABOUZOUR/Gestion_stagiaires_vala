import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Encadrant } from '../services/encadrant';
import { Stage } from '../services/stage';

@Component({
  selector: 'app-edit-stage',
  standalone: false,
  templateUrl: './edit-stage.html',
  styleUrls: ['./edit-stage.css']
})
export class EditStage implements OnInit {
  editStageFormGroup!: FormGroup;
  stageId!: number;
  encadrantsList: any[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private stageService: Stage,
    private encadrantService: Encadrant
  ) {}

  ngOnInit(): void {
    this.stageId = +this.route.snapshot.paramMap.get('id')!;
    
    this.editStageFormGroup = this.fb.group({
      sujet: ['', Validators.required],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      encadrant: ['', Validators.required],
      status: ['']
    });

    this.encadrantService.getEncadrants().subscribe(data => {
      this.encadrantsList = data;
    });

    this.stageService.getStageById(this.stageId).subscribe(stage => {
      this.editStageFormGroup.patchValue({
        sujet: stage.sujet,
        dateDebut: stage.dateDebut,
        dateFin: stage.dateFin,
        encadrant: stage.encadrant.id,  
        status: stage.status
      });
    });
  }

  handleUpdateStage() {
    if (this.editStageFormGroup.invalid) {
      alert("Veuillez remplir tous les champs obligatoires");
      return;
    }

    const selectedEncadrantId = this.editStageFormGroup.value.encadrant;
    const encadrantObject = this.encadrantsList.find(e => e.id == selectedEncadrantId);

    if (!encadrantObject) {
      alert("Encadrant introuvable");
      return;
    }

    const updatedStage = {
      ...this.editStageFormGroup.value,
      encadrant: encadrantObject
    };

    this.stageService.editStage(this.stageId, updatedStage).subscribe({
      next: () => {
        alert("Le stage a été mis à jour avec succès");
        this.router.navigate(['/stages']);
      },
      error: (err) => {
        console.error(err);
        alert("Une erreur est survenue lors de la mise à jour");
      }
    });
  }
}
