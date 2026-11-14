import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DepartementModel } from '../models/stage.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class Departement {

  constructor(private http: HttpClient) { }
  
  public getDepartements(): Observable<any> {
    return this.http.get(environment.apiUrl + "/departements")
  }
  public getDepartementById(id:number): Observable<any>{
    return this.http.get(environment.apiUrl + "/departements/"+id)
  }
  public searchDepartements(keyword: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/departements/search?keyword=" + keyword)
  }

  public addDepartement(departement:DepartementModel){
    return this.http.post(environment.apiUrl + "/departements",departement);
  }
  public deleteDepartement(id:number){
    return this.http.delete(environment.apiUrl + "/departements/"+id);
  }
  editDepartement(id: number, departement:DepartementModel ) {
  return this.http.put(environment.apiUrl + "/departements/"+id,departement);
}

}