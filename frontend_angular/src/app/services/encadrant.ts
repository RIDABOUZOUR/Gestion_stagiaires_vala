import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

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
}