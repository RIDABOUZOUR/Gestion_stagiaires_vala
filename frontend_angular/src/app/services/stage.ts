import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { StageModel } from '../models/stage.model';

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
 public deleteStage(id: number) {  
  return this.http.delete("http://localhost:8080/stages/"+id); 
  }
public editStage(id : number, stage: StageModel){
  return this.http.put("http://localhost:8080/stages/"+id,stage);
}
public getStageById(id : number):Observable<any>{
  return this.http.get("http://localhost:8080/stages/"+id);
}
}