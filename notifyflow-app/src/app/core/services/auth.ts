import { inject, Injectable } from '@angular/core';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';
import { Observable, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private http = inject(HttpClient);
  private readonly AUTH_URL = 'http://localhost:8080/api/auth';
  private readonly REGISTER_URL = 'http://localhost:8080/api/auth/register';

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.AUTH_URL}/login`, credentials).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('notify_token', response.token);
        }
      })
    );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.REGISTER_URL, data).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('notify_token', response.token);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('notify_token');
  }

  getToken(): string | null {
    return localStorage.getItem('notify_token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}
