import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { error } from 'console';
import { Stagiaire } from '../services/stagiaire';
import { FormBuilder, FormGroup } from '@angular/forms';

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

}
