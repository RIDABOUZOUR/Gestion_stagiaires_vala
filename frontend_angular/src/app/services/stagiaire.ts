import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { StagiaireModel } from '../models/stage.model';

@Injectable({
  providedIn: 'root'
})
export class Stagiaire {

  constructor(private http:HttpClient) { }
  public getStagiaires(): Observable<any>{
    return this.http.get("http://localhost:8080/stagiaires");
  }

   public searchStagiaires(keyword:string): Observable<any>{
    return this.http.get("http://localhost:8080/stagiaires/search?keyword="+keyword)
  }

  public addStagiaire(stagiaire:StagiaireModel){
    return this.http.post("http://localhost:8080/stagiaires",stagiaire);
  }

  public deleteStagiaire(id:number){
    return this.http.delete("http://localhost:8080/stagiaires/"+id);
  }
  public editStagiaire(id:number, stagiaire: StagiaireModel){
    return this.http.put("http://localhost:8080/stagiaires/"+id,stagiaire);
  }
  public getStagiaireById(id:number): Observable<any>{
    return this.http.get("http://localhost:8080/stagiaires/"+id);
  }
} 
