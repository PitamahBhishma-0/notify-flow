import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NotificationRequest, NotificationResponse } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class Notification {
  private http = inject(HttpClient);
  
  private readonly API_URL = 'http://localhost:8080/api/notifications';

  sendNotification(data: NotificationRequest): Observable<NotificationResponse> {
    return this.http.post<NotificationResponse>(this.API_URL, data);
  }
}