import { inject, Injectable } from '@angular/core';
import { AuthResponse, LoginRequest } from '../models/auth.model';
import { Observable, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { response } from 'express';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private http = inject(HttpClient);
  private readonly AUTH_URL = 'http://localhost:8080/api/auth/login';

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.AUTH_URL, credentials).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('notify_token', response.token);
        }
      })
    );
  }
}
