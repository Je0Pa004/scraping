import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiV1 = environment.apiV1;

  constructor(private http: HttpClient) {}

  getProfile(): Observable<User> {
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : null;
    const id = user?.id || user?.user?.id;
    return this.http.get<User>(`${this.apiV1}/users/${id}`);
  }

  updateProfile(user: Partial<User>): Observable<User> {
    const userStr = localStorage.getItem('user');
    const current = userStr ? JSON.parse(userStr) : null;
    const id = current?.id || current?.user?.id;
    return this.http.put<User>(`${this.apiV1}/users/${id}`, user);
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiV1}/users`);
  }

  updateUser(userId: string, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiV1}/users/${userId}`, user);
  }

  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiV1}/users/${userId}`);
  }
}
