import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { error } from 'console';
import { Encadrant } from '../services/encadrant';
import { FormBuilder, FormGroup } from '@angular/forms';
import { EncadrantModel } from '../models/stage.model';

@Component({
  selector: 'app-encadrants',
  standalone: false,
  templateUrl: './encadrants.html',
  styleUrl: './encadrants.css'
})
export class Encadrants implements OnInit {

  encadrants : any;
  searchFormGroup! : FormGroup ;
  constructor(private encadrantService:Encadrant,private fb:FormBuilder ){}
  ngOnInit(): void {
    this.searchFormGroup=this.fb.group({
       keyword:this.fb.control("")
    });
   this.encadrantService.getEncadrants().subscribe({
    next : (data)=>{
      this.encadrants=data;
    },
    error:(err)=>{
      console.log(err);
    }
   }); 
   
  }
  handleSearchEncadrants() {
    let kw = this.searchFormGroup?.value.keyword;
    this.encadrants=this.encadrantService.searchEncadrants(kw).subscribe({
    next : (data)=>{
      this.encadrants=data;
    },
    error:(err)=>{
      console.log(err);
    }
   }); 
  }


handleDeleteEncadrant(encadrant: EncadrantModel) {
  if (confirm(`Voulez-vous vraiment supprimer l'encadrant "${encadrant.nom} ${encadrant.prenom}" ?`)){
    this.encadrantService.deleteEncadrant(encadrant.id).subscribe({
      next:()=>{
        alert(`l'encadrant "${encadrant.nom} ${encadrant.prenom}" a été supprimé avec succès`);
        window.location.reload(); 
      },
        error: (err) => {
          console.error('Erreur suppression:', err);
          alert(`Erreur lors de la suppression : ${err.message || 'Problème serveur'}`);
        }
    })
  }
}




}