import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DepartementModel } from '../models/stage.model';

@Injectable({
  providedIn: 'root'
})
export class Departement {

  constructor(private http: HttpClient) { }
  
  public getDepartements(): Observable<any> {
    return this.http.get("http://localhost:8080/departements")
  }
  public getDepartementById(id:number): Observable<any>{
    return this.http.get("http://localhost:8080/departements/"+id)
  }
  public searchDepartements(keyword: string): Observable<any> {
    return this.http.get("http://localhost:8080/departements/search?keyword=" + keyword)
  }

  public addDepartement(departement:DepartementModel){
    return this.http.post("http://localhost:8080/departements",departement);
  }
  public deleteDepartement(id:number){
    return this.http.delete("http://localhost:8080/departements/"+id);
  }
  editDepartement(id: number, departement:DepartementModel ) {
  return this.http.put("http://localhost:8080/departements/"+id,departement);
}

}