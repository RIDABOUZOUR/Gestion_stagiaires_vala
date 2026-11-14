import { Component, OnInit } from '@angular/core';
import { Departement } from '../services/departement';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-new-deapartement',
  standalone: false,
  templateUrl: './new-deapartement.html',
  styleUrl: './new-deapartement.css'
})
export class NewDeapartement implements OnInit {
  departementsList: any[] = [];
  newDepartementFormGroup! : FormGroup;
  constructor(private fb:FormBuilder,private departementService:Departement){}

  ngOnInit(): void {
     this.newDepartementFormGroup=this.fb.group({
      nom: ['', Validators.required],
      responsable: ['', Validators.required],
    });
  }
  handleSaveDepartement() {
  if (this.newDepartementFormGroup.invalid) {
    alert("Veuillez remplir tous les champs obligatoires");
    return;
  }
  const departement = {
    ...this.newDepartementFormGroup.value,
    
  };

  this.departementService.addDepartement(departement).subscribe({
    next: () => {
      alert("Le département a été enregistré avec succès");
      this.newDepartementFormGroup.reset();
    },
    error: (err) => {
      console.error(err);
      alert("Une erreur est survenue lors de l'enregistrement");
    }
  });

}
}
