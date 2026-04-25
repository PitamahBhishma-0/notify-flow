import { inject, Injectable, signal, DestroyRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { interval, startWith, switchMap, catchError, of, Subscription } from 'rxjs';

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
  private pollingSubscription: Subscription | null = null;

  // Single source of truth for the whole UI
  health = signal<SystemHealth>({
    overall: 'PENDING',
    services: { notification: false, user: false, delivery: false }
  });

  constructor() {
    // Don't auto-start polling - let components explicitly start it
  }

  startPolling(destroyRef: DestroyRef): void {
    if (this.pollingSubscription) {
      return; // Already polling
    }

    this.pollingSubscription = interval(15000).pipe(
      startWith(0),
      switchMap(() => this.http.get<any>(this.HEALTH_URL).pipe(
        catchError((error) => {
          if (error.status === 503 && error.error) {
            return of(error.error);
          }
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

    // Clean up when the component that started polling is destroyed
    destroyRef.onDestroy(() => {
      this.stopPolling();
    });
  }

  stopPolling(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
      this.pollingSubscription = null;
    }
  }
}