import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface QuotaDto { total: number; utilise: number; restant: number; }

@Injectable({ providedIn: 'root' })
export class QuotasService {
  private readonly api = environment.api;
  constructor(private http: HttpClient) {}
  getQuota(): Observable<QuotaDto> {
    return this.http.get<QuotaDto>(`${this.api}/abonnements/quota`);
  }
}
