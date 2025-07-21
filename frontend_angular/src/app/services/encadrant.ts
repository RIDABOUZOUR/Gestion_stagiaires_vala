import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EncadrantModel } from '../models/stage.model';

@Injectable({
  providedIn: 'root'
})
export class Encadrant {  

  constructor(private http: HttpClient) { }
  
  public getEncadrants(): Observable<any> {  
    return this.http.get("http://localhost:8080/encadrants")  
  }

  public searchEncadrants(keyword: string): Observable<any> {  
    return this.http.get("http://localhost:8080/encadrants/search?keyword=" + keyword)  
  }
  public addEncadrant(encadrant:EncadrantModel){
    return this.http.post("http://localhost:8080/encadrants",encadrant)
  }
  public deleteEncadrant(id : number){
    return this.http.delete("http://localhost:8080/encadrants/"+id);
  }
  public getEncadrantById(id : number):Observable<any>{
    return this.http.get("http://localhost:8080/encadrants/"+id);
  }
  public editEncadrant(id : number,encadrant:EncadrantModel){
    return this.http.put("http://localhost:8080/encadrants/"+id,encadrant);
  }
}