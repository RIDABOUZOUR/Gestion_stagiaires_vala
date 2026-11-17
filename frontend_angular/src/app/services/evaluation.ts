import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Evaluation, EvaluationRequest } from '../models/suivi.model';

@Injectable({ providedIn: 'root' })
export class EvaluationService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  evaluation(stagiaireId: number): Observable<Evaluation> {
    return this.http.get<Evaluation>(`${this.api}/stagiaires/${stagiaireId}/evaluation`);
  }

  enregistrer(stagiaireId: number, evaluation: EvaluationRequest): Observable<Evaluation> {
    return this.http.put<Evaluation>(`${this.api}/stagiaires/${stagiaireId}/evaluation`, evaluation);
  }

  attestation(stagiaireId: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.api}/stagiaires/${stagiaireId}/attestation`, {
      observe: 'response',
      responseType: 'blob',
    });
  }
}
