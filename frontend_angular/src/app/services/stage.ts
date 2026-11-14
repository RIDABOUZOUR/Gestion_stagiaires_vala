import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { StageModel } from '../models/stage.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class Stage {  

  constructor(private http:HttpClient) { }
  
  public getStages(): Observable<any>{  
    return this.http.get(environment.apiUrl + "/stages")  ;
  }

  public searchStages(keyword:string): Observable<any>{ 
    return this.http.get(environment.apiUrl + "/stages/search?keyword="+keyword)  ;
  }

  public addStage(stage:Stage){
     return this.http.post(environment.apiUrl + "/stages",stage);
  }
 public deleteStage(id: number) {  
  return this.http.delete(environment.apiUrl + "/stages/"+id); 
  }
public editStage(id : number, stage: StageModel){
  return this.http.put(environment.apiUrl + "/stages/"+id,stage);
}
public getStageById(id : number):Observable<any>{
  return this.http.get(environment.apiUrl + "/stages/"+id);
}
}