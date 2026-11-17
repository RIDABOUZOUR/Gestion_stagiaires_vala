import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { StageModel } from '../models/stage.model';
import {
  AssiduiteStatistiques,
  CandidatureStatistiques,
  DureesEtTaux,
  EvaluationStatistiques,
  ProfilStagiaires,
  RapportEnRetard,
  Repartition,
  ResumeStatistiques,
  StagesParStatut,
} from '../models/stats.model';

@Injectable({ providedIn: 'root' })
export class StatistiquesService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/statistiques`;

  repartition(chemin: 'departements' | 'filieres' | 'organismes' | 'encadrants'): Observable<Repartition> {
    return this.http.get<Repartition>(`${this.base}/${chemin}`);
  }

  resume(): Observable<ResumeStatistiques> {
    return this.http.get<ResumeStatistiques>(`${this.base}/resume`);
  }

  stagesParStatut(): Observable<StagesParStatut> {
    return this.http.get<StagesParStatut>(`${this.base}/stages/statuts`);
  }

  stagesAEcheance(jours = 30): Observable<StageModel[]> {
    return this.http.get<StageModel[]>(`${this.base}/stages/echeances`, { params: { jours } });
  }

  dureesEtTaux(): Observable<DureesEtTaux> {
    return this.http.get<DureesEtTaux>(`${this.base}/stages/durees-et-taux`);
  }

  profil(departementId?: number | null): Observable<ProfilStagiaires> {
    const params: Record<string, number> = departementId ? { departementId } : {};
    return this.http.get<ProfilStagiaires>(`${this.base}/stagiaires/profil`, { params });
  }

  assiduite(): Observable<AssiduiteStatistiques> {
    return this.http.get<AssiduiteStatistiques>(`${this.base}/suivi/assiduite`);
  }

  rapportsEnRetard(jours = 14): Observable<RapportEnRetard[]> {
    return this.http.get<RapportEnRetard[]>(`${this.base}/suivi/rapports-en-retard`, { params: { jours } });
  }

  evaluations(): Observable<EvaluationStatistiques> {
    return this.http.get<EvaluationStatistiques>(`${this.base}/evaluations`);
  }

  candidatures(): Observable<CandidatureStatistiques> {
    return this.http.get<CandidatureStatistiques>(`${this.base}/candidatures`);
  }
}
