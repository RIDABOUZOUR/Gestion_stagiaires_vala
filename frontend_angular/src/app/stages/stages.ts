import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { error } from 'console';
import { Stage } from '../services/stage';  
import { FormBuilder, FormGroup } from '@angular/forms';
import { StageModel } from '../models/stage.model';  



@Component({
  selector: 'app-stages',  
  standalone: false,
  templateUrl: './stages.html',  
  styleUrl: './stages.css'     
})
export class Stages implements OnInit {



  stages : any;  
  searchFormGroup! : FormGroup;
  stagiaires: any;
  constructor(private stageService:Stage, private fb:FormBuilder ){}  
  
  ngOnInit(): void {
    this.searchFormGroup=this.fb.group({
       keyword:this.fb.control("")
    });
   this.stageService.getStages().subscribe({  
    next : (data)=>{
      this.stages=data;  
    },
    error:(err)=>{
      console.log(err);
    }
   }); 
   
  }
  
  handleSearchStages() {  
    let kw = this.searchFormGroup?.value.keyword;
    this.stages=this.stageService.searchStages(kw).subscribe({  
    next : (data)=>{
      this.stages=data;  
    },
    error:(err)=>{
      console.log(err);
    }
   }); 
  }
handleDeleteStage(stage: StageModel) {
  if (confirm(`Voulez-vous vraiment supprimer le stage "${stage.sujet}" ?`)) {
    this.stageService.deleteStage(stage.id).subscribe({
      next: () => {
        alert(`Le stage "${stage.sujet}" a été supprimé avec succès`);
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