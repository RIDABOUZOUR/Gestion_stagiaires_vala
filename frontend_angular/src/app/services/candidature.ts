import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Candidature, CandidatureFiltre, CandidatureRequest } from '../models/candidature.model';
import { PageResponse } from '../models/page.model';
import { parametres } from '../shared/telechargement';

@Injectable({ providedIn: 'root' })
export class CandidatureService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/candidatures`;

  rechercher(filtre: CandidatureFiltre, page = 0, size = 20): Observable<PageResponse<Candidature>> {
    return this.http.get<PageResponse<Candidature>>(`${this.baseUrl}/page`, {
      params: parametres({ keyword: filtre.keyword, statut: filtre.statut, departementId: filtre.departementId, page, size }),
    });
  }

  candidature(id: number): Observable<Candidature> {
    return this.http.get<Candidature>(`${this.baseUrl}/${id}`);
  }

  /** Route publique : utilisable sans être connecté. */
  soumettre(candidature: CandidatureRequest): Observable<Candidature> {
    return this.http.post<Candidature>(this.baseUrl, candidature);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  etudier(id: number): Observable<Candidature> {
    return this.http.patch<Candidature>(`${this.baseUrl}/${id}/etudier`, null);
  }

  refuser(id: number, motif: string): Observable<Candidature> {
    return this.http.patch<Candidature>(`${this.baseUrl}/${id}/refuser`, { motif });
  }

  retirer(id: number): Observable<Candidature> {
    return this.http.patch<Candidature>(`${this.baseUrl}/${id}/retirer`, null);
  }

  accepter(id: number, stageId: number, departementId: number | null): Observable<Candidature> {
    return this.http.post<Candidature>(`${this.baseUrl}/${id}/accepter`, { stageId, departementId });
  }
}
