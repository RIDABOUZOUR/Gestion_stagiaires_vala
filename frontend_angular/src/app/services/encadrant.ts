import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EncadrantModel } from '../models/stage.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class Encadrant {  

  constructor(private http: HttpClient) { }
  
  public getEncadrants(): Observable<any> {  
    return this.http.get(environment.apiUrl + "/encadrants")  
  }

  public searchEncadrants(keyword: string): Observable<any> {  
    return this.http.get(environment.apiUrl + "/encadrants/search?keyword=" + keyword)  
  }
  public addEncadrant(encadrant:EncadrantModel){
    return this.http.post(environment.apiUrl + "/encadrants",encadrant)
  }
  public deleteEncadrant(id : number){
    return this.http.delete(environment.apiUrl + "/encadrants/"+id);
  }
  public getEncadrantById(id : number):Observable<any>{
    return this.http.get(environment.apiUrl + "/encadrants/"+id);
  }
  public editEncadrant(id : number,encadrant:EncadrantModel){
    return this.http.put(environment.apiUrl + "/encadrants/"+id,encadrant);
  }
}