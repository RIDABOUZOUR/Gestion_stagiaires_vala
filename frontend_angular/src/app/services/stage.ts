import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Stage {  

  constructor(private http:HttpClient) { }
  
  public getStages(): Observable<any>{  
    return this.http.get("http://localhost:8080/stages")  ;
  }

  public searchStages(keyword:string): Observable<any>{ 
    return this.http.get("http://localhost:8080/stages/search?keyword="+keyword)  ;
  }

  public addStage(stage:Stage){
     return this.http.post("http://localhost:8080/stages",stage);
  }
}