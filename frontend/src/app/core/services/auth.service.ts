import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { User, LoginRequest, RegisterRequest, AuthResponse } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API = environment.api;      // e.g., http://localhost:8080/api
  private readonly API_V1 = environment.apiV1; // e.g., http://localhost:8080/api/v1
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.loadUserFromStorage();
  }

  // Clear local session without navigating
  public clearSession(): void {
    try {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    } catch {}
    this.currentUserSubject.next(null);
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/auth/login`, credentials)
      .pipe(
        tap(response => {
          this.setSession(response);
        }),
        catchError(this.handleError)
      );
  }

  register(userData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/auth/register`, userData)
      .pipe(
        tap(response => {
          this.setSession(response);
        }),
        catchError(this.handleError)
      );
  }

  // v1-compatible auth used by current UI
  loginV1(credentials: { email: string; motDePasse: string }): Observable<any> {
    return this.http.post<any>(`${this.API_V1}/login`, credentials).pipe(
      tap(resp => this.setSessionFromV1(resp)),
      catchError(err => {
        // Fallback to /api/auth/login if v1 endpoint is not available
        return this.http.post<any>(`${this.API}/auth/login`, credentials).pipe(
          tap(resp => this.setSessionFromV1(resp)),
          catchError(this.handleError)
        );
      })
    );
  }

  registerV1(payload: { nomComplet?: string; email: string; motDePasse: string; entreprise?: string; typeCompte?: 'USER' | 'RECRUTEUR' }): Observable<any> {
    return this.http.post<any>(`${this.API_V1}/register`, payload).pipe(
      catchError(err => {
        // Fallback to /api/auth/register
        return this.http.post<any>(`${this.API}/auth/register`, payload).pipe(
          catchError(this.handleError)
        );
      })
    );
  }

  logout(): void {
    this.clearSession();
    this.router.navigate(['/login']);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.API}/auth/forgot-password`, { email })
      .pipe(catchError(this.handleError));
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<AuthResponse>(`${this.API}/auth/refresh`, { refreshToken })
      .pipe(
        tap(response => {
          this.setSession(response);
        }),
        catchError(this.handleError)
      );
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  hasRole(role: string): boolean {
    const user: any = this.getCurrentUser();
    if (!user) return false;
    const raw = user?.roles ?? user?.role ?? [];
    const list: string[] = Array.isArray(raw) ? raw : (typeof raw === 'string' ? raw.split(',') : []);
    const normalized = list.map((r: string) => String(r).trim().toUpperCase()).filter(Boolean);
    const withPrefix = new Set(normalized.map(r => r.startsWith('ROLE_') ? r : `ROLE_${r}`));
    const want = String(role).trim().toUpperCase();
    const wantPref = want.startsWith('ROLE_') ? want : `ROLE_${want}`;
    return withPrefix.has(wantPref);
  }

  private setSession(authResult: AuthResponse): void {
    localStorage.setItem('token', authResult.token);
    localStorage.setItem('refreshToken', authResult.refreshToken);
    localStorage.setItem('user', JSON.stringify(authResult.user));
    this.currentUserSubject.next(authResult.user);
  }

  private setSessionFromV1(resp: any): void {
    if (resp?.token) {
      localStorage.setItem('token', resp.token);
    }
    if (resp?.refreshToken) {
      localStorage.setItem('refreshToken', resp.refreshToken);
    }
    if (resp) {
      localStorage.setItem('user', JSON.stringify(resp));
    }
    // Try to project to User if structure matches
    try {
      const u = resp?.user || resp;
      if (u) this.currentUserSubject.next(u as User);
    } catch {}
  }

  private loadUserFromStorage(): void {
    const user = localStorage.getItem('user');
    if (user) {
      try {
        this.currentUserSubject.next(JSON.parse(user));
      } catch (error) {
        this.logout();
      }
    }
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    // Log and pass through the original HttpErrorResponse to preserve status/body
    console.error('HTTP Error', error);
    return throwError(() => error);
  }
}
