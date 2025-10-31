import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { LeadProfileRef } from '../models/lead.model';

export interface SendMessagePayload {
  toEmail?: string;
  toName?: string;
  subject: string;
  body: string;
  scheduledAt?: string; // ISO string
  profile?: LeadProfileRef;
}

@Injectable({ providedIn: 'root' })
export class MessagesService {
  private readonly api = environment.api;

  constructor(private http: HttpClient) {}

  send(payload: SendMessagePayload): Observable<{ id: string; status: string }> {
    return this.http.post<{ id: string; status: string }>(`${this.api}/messages/send`, payload);
  }

  generateText(payload: { name?: string; skills?: string; company?: string; role?: string; }): Observable<{ text: string }> {
    // Try /api first
    return this.http.post<{ text: string }>(`${this.api}/ai/generate-message`, payload).pipe(
      catchError(() => {
        // Fallback to /api/v1
        return this.http.post<{ text: string }>(`${environment.apiV1}/ai/generate-message`, payload).pipe(
          catchError(() => {
            // Final fallback: generate a basic template client-side
            const name = payload.name?.split(' ')[0] || 'Candidat';
            const skill = (payload.skills || '').split(',')[0]?.trim() || 'votre domaine';
            const text = `Bonjour ${name},\n\nJ'ai découvert votre profil et vos compétences en ${skill}. Nous serions ravis d'échanger sur une opportunité correspondant à votre parcours.\n\nSeriez-vous disponible pour un court échange cette semaine ?\n\nBien cordialement,`;
            return of({ text });
          })
        );
      })
    );
  }
}
