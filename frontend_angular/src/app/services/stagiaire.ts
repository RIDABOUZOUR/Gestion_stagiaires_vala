import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { StagiaireModel } from '../models/stage.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class Stagiaire {

  constructor(private http:HttpClient) { }
  public getStagiaires(): Observable<any>{
    return this.http.get(environment.apiUrl + "/stagiaires");
  }

   public searchStagiaires(keyword:string): Observable<any>{
    return this.http.get(environment.apiUrl + "/stagiaires/search?keyword="+keyword)
  }

  public addStagiaire(stagiaire:StagiaireModel){
    return this.http.post(environment.apiUrl + "/stagiaires",stagiaire);
  }

  public deleteStagiaire(id:number){
    return this.http.delete(environment.apiUrl + "/stagiaires/"+id);
  }
  public editStagiaire(id:number, stagiaire: StagiaireModel){
    return this.http.put(environment.apiUrl + "/stagiaires/"+id,stagiaire);
  }
  public getStagiaireById(id:number): Observable<any>{
    return this.http.get(environment.apiUrl + "/stagiaires/"+id);
  }
} 
