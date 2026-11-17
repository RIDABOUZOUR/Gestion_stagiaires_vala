import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { StagiaireModel } from '../models/stage.model';
import { PageResponse } from '../models/page.model';
import { parametres } from '../shared/telechargement';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class Stagiaire {
  private readonly baseUrl = `${environment.apiUrl}/stagiaires`;

  constructor(private readonly http: HttpClient) {}

  getStagiaires(): Observable<StagiaireModel[]> {
    return this.http.get<StagiaireModel[]>(this.baseUrl);
  }

  searchStagiaires(keyword: string): Observable<StagiaireModel[]> {
    return this.http.get<StagiaireModel[]>(
      `${this.baseUrl}/search?keyword=${encodeURIComponent(keyword)}`,
    );
  }

  getStagiaireById(id: number): Observable<StagiaireModel> {
    return this.http.get<StagiaireModel>(`${this.baseUrl}/${id}`);
  }

  addStagiaire(stagiaire: StagiaireModel): Observable<StagiaireModel> {
    return this.http.post<StagiaireModel>(this.baseUrl, stagiaire);
  }

  editStagiaire(id: number, stagiaire: StagiaireModel): Observable<StagiaireModel> {
    return this.http.put<StagiaireModel>(`${this.baseUrl}/${id}`, stagiaire);
  }

  deleteStagiaire(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  /** Recherche multicritère paginée (mêmes filtres que l'export CSV). */
  rechercher(
    filtres: Record<string, string | number | null | undefined>,
    page = 0,
    size = 20,
  ): Observable<PageResponse<StagiaireModel>> {
    return this.http.get<PageResponse<StagiaireModel>>(`${this.baseUrl}/page`, {
      params: parametres({ ...filtres, page, size }),
    });
  }
}
