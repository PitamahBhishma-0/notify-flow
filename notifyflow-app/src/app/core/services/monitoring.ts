import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { interval, startWith, switchMap, catchError, of } from 'rxjs';

export interface SystemHealth {
  overall: 'UP' | 'DOWN' | 'DEGRADED' | 'PENDING';
  services: {
    notification: boolean;
    user: boolean;
    delivery: boolean;
  };
}

@Injectable({ providedIn: 'root' })
export class Monitoring {
  private http = inject(HttpClient);
  private readonly HEALTH_URL = 'http://localhost:8080/actuator/health';

  // Single source of truth for the whole UI
  health = signal<SystemHealth>({
    overall: 'PENDING',
    services: { notification: false, user: false, delivery: false }
  });

  constructor() {
    interval(15000).pipe( // 15s is standard for health checks
      startWith(0),
      switchMap(() => this.http.get<any>(this.HEALTH_URL).pipe(
        catchError((error) => {
    // If we get a 503, we might still have the JSON body!
    if (error.status === 503 && error.error) {
      return of(error.error); 
    }
    // If the server is totally dead (0) or other errors
    return of({ status: 'OFFLINE' });
  })
      ))
    ).subscribe(res => {
      if (res.status === 'OFFLINE') {
        this.health.set({ overall: 'DOWN', services: { notification: false, user: false, delivery: false } });
        return;
      }

      const services = res.components;
      const notificationUp = services['notification-service']?.status === 'UP';
      const userUp = services['user-service']?.status === 'UP';
      const deliveryUp = services['delivery-service']?.status === 'UP';

      // Define "DEGRADED" if Gateway is UP but any sub-service is DOWN
      const isDegraded = !notificationUp || !userUp || !deliveryUp;

      this.health.set({
        overall: isDegraded ? 'DEGRADED' : 'UP',
        services: {
          notification: notificationUp,
          user: userUp,
          delivery: deliveryUp
        }
      });
    });
  }
}