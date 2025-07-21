import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { error } from 'console';
import { Stagiaire } from '../services/stagiaire';
import { FormBuilder, FormGroup } from '@angular/forms';
import { StagiaireModel } from '../models/stage.model';

@Component({
  selector: 'app-stagiaires',
  standalone: false,
  templateUrl: './stagiaires.html',
  styleUrl: './stagiaires.css'
})
export class Stagiaires implements OnInit {

  stagiaires : any;
  searchFormGroup! : FormGroup ;
  constructor(private stagiaireService:Stagiaire,private fb:FormBuilder ){}
  ngOnInit(): void {
    this.searchFormGroup=this.fb.group({
       keyword:this.fb.control("")
    });
   this.stagiaireService.getStagiaires().subscribe({
    next : (data)=>{
      this.stagiaires=data;
    },
    error:(err)=>{
      console.log(err);
    }
   }); 
   
  }
  handleSearchStagiaires() {
    let kw = this.searchFormGroup?.value.keyword;
    this.stagiaires=this.stagiaireService.searchStagiaires(kw).subscribe({
    next : (data)=>{
      this.stagiaires=data;
    },
    error:(err)=>{
      console.log(err);
    }
   }); 
  }

  handleDeleteStagiaire(stagiaire: StagiaireModel) {
  if (confirm(`Voulez-vous vraiment supprimer le stagiaire "${stagiaire.prenom} ${stagiaire.nom}" ?`)) {
    this.stagiaireService.deleteStagiaire(stagiaire.id).subscribe({
      next: () => {
        alert(`Le stagiaire "${stagiaire.prenom} ${stagiaire.nom}" a été supprimé avec succès`);
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
