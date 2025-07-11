import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Departement {

  constructor(private http: HttpClient) { }
  
  public getDepartements(): Observable<any> {
    return this.http.get("http://localhost:8080/departements")
  }

  public searchDepartements(keyword: string): Observable<any> {
    return this.http.get("http://localhost:8080/departements/search?keyword=" + keyword)
  }
}