import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Candidat } from '../models/candidat.model';

@Injectable({ providedIn: 'root' })
export class CandidatsService {
  private readonly api = environment.api;       // e.g. http://localhost:8080/api
  private readonly apiV1 = environment.apiV1;   // e.g. http://localhost:8080/api/v1

  constructor(private http: HttpClient) {}

  getAll(): Observable<Candidat[]> {
    return this.http.get<Candidat[]>(`${this.api}/candidats`);
  }

  inviter(payload: { email: string; nom?: string; telephone?: string; profilPublicId?: string; notes?: string }): Observable<Candidat> {
    // Endpoint attendu côté backend: POST /api/candidats/invitations
    return this.http.post<Candidat>(`${this.api}/candidats/invitations`, payload);
  }

  uploadCv(token: string, file: File): Observable<Candidat> {
    const form = new FormData();
    form.append('file', file);
    const params = new HttpParams().set('token', token);
    // Endpoint public attendu côté backend: POST /api/public/candidats/upload-cv?token=...
    return this.http.post<Candidat>(`${this.api}/public/candidats/upload-cv`, form, { params });
  }
}
