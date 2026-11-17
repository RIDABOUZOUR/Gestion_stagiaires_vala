import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { StageModel } from '../models/stage.model';
import { StageHistorique } from '../models/suivi.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class Stage {
  private readonly baseUrl = `${environment.apiUrl}/stages`;

  constructor(private readonly http: HttpClient) {}

  getStages(): Observable<StageModel[]> {
    return this.http.get<StageModel[]>(this.baseUrl);
  }

  searchStages(keyword: string): Observable<StageModel[]> {
    return this.http.get<StageModel[]>(
      `${this.baseUrl}/search?keyword=${encodeURIComponent(keyword)}`,
    );
  }

  getStageById(id: number): Observable<StageModel> {
    return this.http.get<StageModel>(`${this.baseUrl}/${id}`);
  }

  addStage(stage: StageModel): Observable<StageModel> {
    return this.http.post<StageModel>(this.baseUrl, stage);
  }

  editStage(id: number, stage: StageModel): Observable<StageModel> {
    return this.http.put<StageModel>(`${this.baseUrl}/${id}`, stage);
  }

  deleteStage(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getHistorique(id: number): Observable<StageHistorique[]> {
    return this.http.get<StageHistorique[]>(`${this.baseUrl}/${id}/historique`);
  }

  /** Action de cycle de vie : demarrer, terminer ou annuler. */
  changerStatut(id: number, action: 'demarrer' | 'terminer' | 'annuler'): Observable<StageModel> {
    return this.http.patch<StageModel>(`${this.baseUrl}/${id}/${action}`, null);
  }
}
