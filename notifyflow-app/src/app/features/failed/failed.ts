import { Component, inject, signal, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HttpClient } from '@angular/common/http';
import { interval, startWith, switchMap, catchError, of } from 'rxjs';

export interface FailedDelivery {
  id: string;
  recipientUserId: string;
  channel: 'EMAIL' | 'SMS' | 'IN_APP';
  subject: string;
  failureReason: string;
  retryCount: number;
  lastAttempt: string;
  status: 'FAILED' | 'RETRYING' | 'DLQ';
}

@Component({
  selector: 'app-failed',
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule
  ],
  standalone: true,
  templateUrl: './failed.html',
  styleUrl: './failed.scss',
})
export default class Failed {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private readonly API_URL = 'http://localhost:8080/api/delivery/failed';

  displayedColumns: string[] = ['id', 'recipient', 'channel', 'subject', 'reason', 'retries', 'lastAttempt', 'actions'];
  failedDeliveries = signal<FailedDelivery[]>([]);
  isLoading = signal(true);
  totalFailed = signal(0);

  constructor() {
    this.loadFailedDeliveries();
    // Poll every 30 seconds for updates
    const subscription = interval(30000).pipe(
      startWith(0),
      switchMap(() => this.fetchFailedDeliveries())
    ).subscribe();

    // Clean up subscription when component is destroyed
    this.destroyRef.onDestroy(() => {
      subscription.unsubscribe();
    });
  }

  private loadFailedDeliveries(): void {
    this.fetchFailedDeliveries().subscribe();
  }

  private fetchFailedDeliveries() {
    return this.http.get<FailedDelivery[]>(this.API_URL).pipe(
      catchError((error) => {
        console.error('Failed to fetch failed deliveries:', error);
        // Use mock data if API fails
        this.failedDeliveries.set(this.getMockData());
        this.totalFailed.set(this.getMockData().length);
        this.isLoading.set(false);
        return of(this.getMockData());
      })
    );
  }

  retryDelivery(id: string): void {
    this.http.post(`${this.API_URL}/${id}/retry`, {}).subscribe({
      next: () => {
        this.loadFailedDeliveries();
      },
      error: (error) => {
        console.error('Failed to retry delivery:', error);
      }
    });
  }

  moveToDlq(id: string): void {
    this.http.post(`${this.API_URL}/${id}/dlq`, {}).subscribe({
      next: () => {
        this.loadFailedDeliveries();
      },
      error: (error) => {
        console.error('Failed to move to DLQ:', error);
      }
    });
  }

  getChannelIcon(channel: string): string {
    switch (channel) {
      case 'EMAIL': return 'email';
      case 'SMS': return 'sms';
      case 'IN_APP': return 'notifications';
      default: return 'error';
    }
  }

  getChannelClass(channel: string): string {
    switch (channel) {
      case 'EMAIL': return 'channel-email';
      case 'SMS': return 'channel-sms';
      case 'IN_APP': return 'channel-inapp';
      default: return '';
    }
  }

  private getMockData(): FailedDelivery[] {
    return [
      {
        id: 'notif_8f3k2x9m',
        recipientUserId: 'usr_abc123',
        channel: 'EMAIL',
        subject: 'Payment confirmation',
        failureReason: 'SMTP connection timeout',
        retryCount: 3,
        lastAttempt: '2024-01-15T14:30:00Z',
        status: 'FAILED'
      },
      {
        id: 'notif_9j4l3y0n',
        recipientUserId: 'usr_def456',
        channel: 'SMS',
        subject: 'OTP verification',
        failureReason: 'Invalid phone number format',
        retryCount: 1,
        lastAttempt: '2024-01-15T14:25:00Z',
        status: 'DLQ'
      },
      {
        id: 'notif_0k5m4z1o',
        recipientUserId: 'usr_ghi789',
        channel: 'IN_APP',
        subject: 'New message alert',
        failureReason: 'User not found',
        retryCount: 2,
        lastAttempt: '2024-01-15T14:20:00Z',
        status: 'RETRYING'
      }
    ];
  }
}
