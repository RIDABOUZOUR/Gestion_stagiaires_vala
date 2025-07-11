import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { error } from 'console';
import { Stage } from '../services/stage';  
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-stages',  
  standalone: false,
  templateUrl: './stages.html',  
  styleUrl: './stages.css'     
})
export class Stages implements OnInit {
handleSearchStagiaires() {
throw new Error('Method not implemented.');
}  

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

}