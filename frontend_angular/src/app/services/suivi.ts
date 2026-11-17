import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Presence, Rapport, RapportRequest, StatutPresence } from '../models/suivi.model';
import { parametres } from '../shared/telechargement';

/** Présences et rapports hebdomadaires d'un stagiaire. */
@Injectable({ providedIn: 'root' })
export class SuiviService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  presences(stagiaireId: number, du?: string, au?: string): Observable<Presence[]> {
    return this.http.get<Presence[]>(`${this.api}/stagiaires/${stagiaireId}/presences`, {
      params: parametres({ du, au }),
    });
  }

  enregistrerPresence(stagiaireId: number, date: string, statut: StatutPresence, commentaire: string | null = null): Observable<Presence> {
    return this.http.put<Presence>(`${this.api}/stagiaires/${stagiaireId}/presences/${date}`, { statut, commentaire });
  }

  supprimerPresence(stagiaireId: number, date: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/stagiaires/${stagiaireId}/presences/${date}`);
  }

  rapports(stagiaireId: number): Observable<Rapport[]> {
    return this.http.get<Rapport[]>(`${this.api}/stagiaires/${stagiaireId}/rapports`);
  }

  deposerRapport(stagiaireId: number, rapport: RapportRequest): Observable<Rapport> {
    return this.http.post<Rapport>(`${this.api}/stagiaires/${stagiaireId}/rapports`, rapport);
  }

  modifierRapport(id: number, rapport: RapportRequest): Observable<Rapport> {
    return this.http.put<Rapport>(`${this.api}/rapports/${id}`, rapport);
  }

  validerRapport(id: number, commentaire: string | null): Observable<Rapport> {
    return this.http.patch<Rapport>(`${this.api}/rapports/${id}/valider`, { commentaire });
  }

  demanderRevision(id: number, commentaire: string): Observable<Rapport> {
    return this.http.patch<Rapport>(`${this.api}/rapports/${id}/a-revoir`, { commentaire });
  }
}
