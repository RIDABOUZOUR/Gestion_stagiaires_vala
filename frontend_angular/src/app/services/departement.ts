import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DepartementModel } from '../models/stage.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class Departement {
  private readonly baseUrl = `${environment.apiUrl}/departements`;

  constructor(private readonly http: HttpClient) {}

  getDepartements(): Observable<DepartementModel[]> {
    return this.http.get<DepartementModel[]>(this.baseUrl);
  }

  searchDepartements(keyword: string): Observable<DepartementModel[]> {
    return this.http.get<DepartementModel[]>(
      `${this.baseUrl}/search?keyword=${encodeURIComponent(keyword)}`,
    );
  }

  getDepartementById(id: number): Observable<DepartementModel> {
    return this.http.get<DepartementModel>(`${this.baseUrl}/${id}`);
  }

  addDepartement(departement: DepartementModel): Observable<DepartementModel> {
    return this.http.post<DepartementModel>(this.baseUrl, departement);
  }

  editDepartement(id: number, departement: DepartementModel): Observable<DepartementModel> {
    return this.http.put<DepartementModel>(`${this.baseUrl}/${id}`, departement);
  }

  deleteDepartement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
