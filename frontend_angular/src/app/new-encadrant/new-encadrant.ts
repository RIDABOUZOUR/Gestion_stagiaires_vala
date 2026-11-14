import { Component, OnInit } from '@angular/core';
import { Encadrant } from '../services/encadrant';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-new-encadrant',
  standalone: false,
  templateUrl: './new-encadrant.html',
  styleUrl: './new-encadrant.css'
})
export class NewEncadrant implements OnInit {
  encadrantsList: any[] = [];
  newEncadrantFormGroup! : FormGroup;
  
  constructor(private fb: FormBuilder, private encadrantService: Encadrant) {}

  ngOnInit(): void {
    this.newEncadrantFormGroup = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['']
    });
  }

  handleSaveEncadrant() {
    if (this.newEncadrantFormGroup.invalid) {
      alert("Veuillez remplir tous les champs obligatoires");
      return;
    }

    const encadrant = {
      ...this.newEncadrantFormGroup.value
    };

    this.encadrantService.addEncadrant(encadrant).subscribe({
      next: () => {
        alert("L'encadrant a été enregistré avec succès");
        this.newEncadrantFormGroup.reset();
      },
      error: (err) => {
        console.error(err);
        alert("Une erreur est survenue lors de l'enregistrement");
      }
    });
  }
}