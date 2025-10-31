import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, of, throwError } from 'rxjs';
import { Subscription, SubscriptionType } from '../models/subscription.model';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly api = environment.api;       // http://localhost:8080/api
  private readonly apiV1 = environment.apiV1;   // http://localhost:8080/api/v1
  private changesSubject = new Subject<void>();
  public readonly changes$ = this.changesSubject.asObservable();

  constructor(private http: HttpClient, private auth: AuthService) {}

  private hasToken(): boolean {
    return !!this.auth.getToken();
  }

  // User methods
  getSubscriptionTypes(): Observable<SubscriptionType[]> {
    return this.http.get<SubscriptionType[]>(`${this.apiV1}/type-abonnements`);
  }

  // Backward compatible alias for admin components
  getAllSubscriptionTypes(): Observable<SubscriptionType[]> {
    return this.getSubscriptionTypes();
  }

  getUserSubscriptions(): Observable<Subscription[]> {
    if (!this.hasToken()) return of([]);
    return this.http.get<Subscription[]>(`${this.api}/abonnements`);
  }

  createSubscription(subscription: Partial<Subscription>): Observable<Subscription> {
    if (!this.hasToken()) return throwError(() => new Error('Not authenticated'));
    return this.http.post<Subscription>(`${this.api}/abonnements`, subscription);
  }

  createPayment(payment: any): Observable<any> {
    if (!this.hasToken()) return throwError(() => new Error('Not authenticated'));
    return this.http.post(`${this.api}/paiements`, payment);
  }

  getPayments(): Observable<any[]> {
    if (!this.hasToken()) return of([]);
    return this.http.get<any[]>(`${this.api}/paiements`);
  }

  // Type d'abonnements (public controller)
  createSubscriptionType(subscriptionType: Partial<SubscriptionType>): Observable<SubscriptionType> {
    return this.http.post<SubscriptionType>(`${this.apiV1}/type-abonnements`, subscriptionType);
  }

  updateSubscriptionType(id: number, subscriptionType: Partial<SubscriptionType>): Observable<SubscriptionType> {
    return this.http.put<SubscriptionType>(`${this.apiV1}/type-abonnements/${id}`, subscriptionType);
  }

  deleteSubscriptionType(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiV1}/type-abonnements/${id}`);
  }

  toggleSubscriptionTypeStatus(id: number): Observable<SubscriptionType> {
    return this.http.patch<SubscriptionType>(`${this.apiV1}/type-abonnements/${id}/toggle-status`, {});
  }

  // Paiements avancés
  updatePaymentStatus(id: string, payload: { status: string; typeAbonnementId?: number }): Observable<any> {
    if (!this.hasToken()) return throwError(() => new Error('Not authenticated'));
    return this.http.put(`${this.api}/paiements/${id}/status`, payload);
  }

  createPaymentIntent(payload: { amount: number; currency: string; typeAbonnementId: number }): Observable<{ clientSecret: string }> {
    if (!this.hasToken()) return throwError(() => new Error('Not authenticated'));
    return this.http.post<{ clientSecret: string }>(`${this.api}/paiements/create-payment-intent`, payload);
  }

  notifyChanged(): void {
    this.changesSubject.next();
  }
}
