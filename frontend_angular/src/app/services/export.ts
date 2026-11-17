import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { parametres } from '../shared/telechargement';

export type CibleExport = 'stagiaires' | 'stages' | 'candidatures';

/** Exports CSV : mêmes filtres que les recherches de l'API. */
@Injectable({ providedIn: 'root' })
export class ExportService {
  private readonly http = inject(HttpClient);

  csv(cible: CibleExport, filtres: Record<string, string | number | null | undefined> = {}): Observable<HttpResponse<Blob>> {
    return this.http.get(`${environment.apiUrl}/${cible}/export.csv`, {
      params: parametres(filtres),
      observe: 'response',
      responseType: 'blob',
    });
  }
}
