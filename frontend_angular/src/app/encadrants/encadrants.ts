import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { error } from 'console';
import { Encadrant } from '../services/encadrant';
import { FormBuilder, FormGroup } from '@angular/forms';

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

}