import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Utilisateur, UtilisateurModification, UtilisateurRequest } from '../models/utilisateur.model';

@Injectable({ providedIn: 'root' })
export class UtilisateurService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/utilisateurs`;

  lister(): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(this.baseUrl);
  }

  creer(utilisateur: UtilisateurRequest): Observable<Utilisateur> {
    return this.http.post<Utilisateur>(this.baseUrl, utilisateur);
  }

  modifier(id: number, modification: UtilisateurModification): Observable<Utilisateur> {
    return this.http.put<Utilisateur>(`${this.baseUrl}/${id}`, modification);
  }

  changerMotDePasse(id: number, motDePasse: string): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/mot-de-passe`, { motDePasse });
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
