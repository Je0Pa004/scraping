import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AdminStats {
  totalUsers: number;
  totalSearches: number;
  totalProfiles: number;
  monthlyRevenue: number;
  freeUsers: number;
  basicUsers: number;
  premiumUsers: number;
}

export interface UserManagement {
  id: string;
  fullName: string;
  email: string;
  roles: string;
  plan: string;
  registrationDate: string;
  searchesUsed: number;
  searchQuota: number;
  quotaPercentage: number;
  status: 'ACTIVE' | 'SUSPENDED';
  phone?: string;
  company?: string;
  emailVerified?: boolean;
  lastLogin?: string;
}

export interface Payment {
  id: string;
  transactionId: string;
  user: string;
  email: string;
  plan: string;
  amount: number;
  date: string;
  status: 'SUCCEEDED' | 'PENDING' | 'FAILED' | 'REFUNDED';
}

export interface ScrapingActivity {
  id: string;
  user: string;
  email: string;
  criteria: string;
  location: string;
  source: string;
  date: string;
  profilesFound: number;
  status: 'running' | 'completed' | 'failed';
}

export interface SystemLog {
  id: string;
  timestamp: string;
  type: 'error' | 'warning' | 'info';
  source: string;
  message: string;
  details: string;
  user?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly apiV1 = environment.apiV1;

  constructor(private http: HttpClient) {}

  // Dashboard Stats
  getAdminStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.apiV1}/admin/stats`);
  }

  // User Management
  getAllUsers(): Observable<UserManagement[]> {
    return this.http.get<UserManagement[]>(`${this.apiV1}/admin/users`);
  }

  suspendUser(userId: string): Observable<void> {
    return this.http.put<void>(`${this.apiV1}/admin/users/${userId}/suspend`, {});
  }

  activateUser(userId: string): Observable<void> {
    return this.http.put<void>(`${this.apiV1}/admin/users/${userId}/activate`, {});
  }

  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiV1}/admin/users/${userId}`);
  }

  updateUser(userId: string, user: any): Observable<any> {
    return this.http.put<any>(`${this.apiV1}/admin/users/${userId}`, user);
  }

  createUser(user: any): Observable<UserManagement> {
    return this.http.post<UserManagement>(`${this.apiV1}/admin/users`, user);
  }

  // Payments
  getAllPayments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiV1}/admin/payments`);
  }

  refundPayment(paymentId: string): Observable<void> {
    return this.http.post<void>(`${this.apiV1}/admin/payments/${paymentId}/refund`, {});
  }

  updatePaymentStatus(paymentId: string, payload: { status: 'SUCCESS' | 'PENDING' | 'FAILED' }): Observable<Payment> {
    return this.http.put<Payment>(`${this.apiV1}/admin/payments/${paymentId}/status`, payload);
  }

  // Scraping Activity
  getAllScrapingActivities(): Observable<ScrapingActivity[]> {
    return this.http.get<ScrapingActivity[]>(`${this.apiV1}/admin/scraping`);
  }

  stopScraping(scrapingId: string): Observable<void> {
    return this.http.post<void>(`${this.apiV1}/admin/scraping/${scrapingId}/stop`, {});
  }

  // Logs
  getSystemLogs(): Observable<SystemLog[]> {
    return this.http.get<SystemLog[]>(`${this.apiV1}/admin/logs`);
  }
}
