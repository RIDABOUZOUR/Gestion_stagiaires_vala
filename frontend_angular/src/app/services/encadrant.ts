import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EncadrantModel } from '../models/stage.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class Encadrant {
  private readonly baseUrl = `${environment.apiUrl}/encadrants`;

  constructor(private readonly http: HttpClient) {}

  getEncadrants(): Observable<EncadrantModel[]> {
    return this.http.get<EncadrantModel[]>(this.baseUrl);
  }

  searchEncadrants(keyword: string): Observable<EncadrantModel[]> {
    return this.http.get<EncadrantModel[]>(
      `${this.baseUrl}/search?keyword=${encodeURIComponent(keyword)}`,
    );
  }

  getEncadrantById(id: number): Observable<EncadrantModel> {
    return this.http.get<EncadrantModel>(`${this.baseUrl}/${id}`);
  }

  addEncadrant(encadrant: EncadrantModel): Observable<EncadrantModel> {
    return this.http.post<EncadrantModel>(this.baseUrl, encadrant);
  }

  editEncadrant(id: number, encadrant: EncadrantModel): Observable<EncadrantModel> {
    return this.http.put<EncadrantModel>(`${this.baseUrl}/${id}`, encadrant);
  }

  deleteEncadrant(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
