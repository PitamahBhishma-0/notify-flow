import { Component, inject, signal, computed, DestroyRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { interval, startWith, switchMap, catchError, of } from 'rxjs';
import { MetricCard } from '../../shared/components/metric-card/metric-card';
import { BarChart } from '../../shared/components/bar-chart/bar-chart';

export interface QueueMetrics {
  high: number;
  medium: number;
  low: number;
  email: number;
  sms: number;
  inApp: number;
  avgWaitTime: number;
  activeWorkers: number;
}

@Component({
  selector: 'app-queue',
  imports: [BarChart, MetricCard],
  standalone: true,
  templateUrl: './queue.html',
  styleUrl: './queue.scss',
})
export default class Queue {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private readonly API_URL = 'http://localhost:8080/api/delivery/queue';

  metrics = signal<QueueMetrics>({
    high: 0,
    medium: 0,
    low: 0,
    email: 0,
    sms: 0,
    inApp: 0,
    avgWaitTime: 0,
    activeWorkers: 0
  });

  isLoading = signal(true);

  // Priority data for bar chart
  priorityData = computed(() => [
    { label: 'High', value: this.metrics().high, color: '#D85A30' },
    { label: 'Medium', value: this.metrics().medium, color: '#BA7517' },
    { label: 'Low', value: this.metrics().low, color: '#639922' }
  ]);

  maxPriority = computed(() => Math.max(...this.priorityData().map(d => d.value), 1));

  // Message type data for bar chart
  messageTypeData = computed(() => [
    { label: 'Email', value: this.metrics().email, color: '#D85A30' },
    { label: 'SMS', value: this.metrics().sms, color: '#BA7517' },
    { label: 'In-App', value: this.metrics().inApp, color: '#639922' }
  ]);

  maxMessageType = computed(() => Math.max(...this.messageTypeData().map(d => d.value), 1));

  // Computed values for metric cards
  avgWaitTimeDisplay = computed(() => `${this.metrics().avgWaitTime.toFixed(1)}s`);
  activeWorkersDisplay = computed(() => this.metrics().activeWorkers.toString());

  constructor() {
    this.loadMetrics();
    // Poll every 15 seconds for real-time updates
    const subscription = interval(15000).pipe(
      startWith(0),
      switchMap(() => this.fetchMetrics())
    ).subscribe();

    // Clean up subscription when component is destroyed
    this.destroyRef.onDestroy(() => {
      subscription.unsubscribe();
    });
  }

  private loadMetrics(): void {
    this.fetchMetrics().subscribe({
      next: (data) => {
        if (data) {
          this.metrics.set(data);
          this.isLoading.set(false);
        }
      }
    });
  }

  private fetchMetrics() {
    return this.http.get<QueueMetrics>(this.API_URL).pipe(
      catchError((error) => {
        console.error('Failed to fetch queue metrics:', error);
        // Use mock data if API fails
        this.metrics.set(this.getMockMetrics());
        this.isLoading.set(false);
        return of(this.getMockMetrics());
      })
    );
  }

  private getMockMetrics(): QueueMetrics {
    return {
      high: 1500,
      medium: 1000,
      low: 100,
      email: 2000,
      sms: 1000,
      inApp: 5000,
      avgWaitTime: 1.2,
      activeWorkers: 8
    };
  }
}
