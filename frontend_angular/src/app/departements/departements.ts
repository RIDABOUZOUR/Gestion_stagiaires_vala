import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { error } from 'console';
import { Departement } from '../services/departement';  
import { FormBuilder, FormGroup } from '@angular/forms';
import { DepartementModel } from '../models/stage.model';

@Component({
  selector: 'app-departements',  
  standalone: false,
  templateUrl: './departements.html',  
  styleUrl: './departements.css'     
})
export class Departements implements OnInit {


  departements : any;  
  searchFormGroup! : FormGroup;

  constructor(private departementService: Departement, private fb: FormBuilder) {}  
  
  ngOnInit(): void {
    this.searchFormGroup = this.fb.group({
       keyword: this.fb.control("")
    });
   this.departementService.getDepartements().subscribe({  
    next: (data) => {
      this.departements = data;  
    },
    error: (err) => {
      console.log(err);
    }
   }); 
  }
  
  handleSearchDepartements() {  
    let kw = this.searchFormGroup?.value.keyword;
    this.departementService.searchDepartements(kw).subscribe({  
    next: (data) => {
      this.departements = data;  
    },
    error: (err) => {
      console.log(err);
    }
   }); 
  }

  handleDeleteDepartement(departement: DepartementModel) {
    if (confirm(`Voulez-vous vraiment supprimer le département "${departement.nom}" ?`)) {
      this.departementService.deleteDepartement(departement.id).subscribe({
        next: () => {
          alert(`Le département "${departement.id}" a été supprimé avec succès`);
          window.location.reload(); 
        },
        error: (err) => {
          console.error('Erreur suppression:', err);
          alert(`Erreur lors de la suppression : ${err.message || 'Problème serveur'}`);
        }
      });
    }
  }
}