import { Component, inject, signal, computed, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { interval, startWith, switchMap, catchError, of } from 'rxjs';
import { MetricCard } from '../../shared/components/metric-card/metric-card';
import { BarChart } from '../../shared/components/bar-chart/bar-chart';

export interface DeliveryMetrics {
  totalSent: number;
  delivered: number;
  failed: number;
  inQueue: number;
  avgDeliveryTime: number;
  successRate: number;
}

export interface ChannelStats {
  channel: 'EMAIL' | 'SMS' | 'IN_APP';
  sent: number;
  delivered: number;
  failed: number;
  avgTime: number;
}

export interface PriorityStats {
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  sent: number;
  delivered: number;
  failed: number;
}

@Component({
  selector: 'app-delivery-status',
  imports: [CommonModule, MetricCard, BarChart],
  standalone: true,
  templateUrl: './delivery-status.html',
  styleUrl: './delivery-status.scss',
})
export default class DeliveryStatus {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);
  private readonly API_URL = 'http://localhost:8080/api/delivery/metrics';

  metrics = signal<DeliveryMetrics>({
    totalSent: 0,
    delivered: 0,
    failed: 0,
    inQueue: 0,
    avgDeliveryTime: 0,
    successRate: 0
  });

  channelStats = signal<ChannelStats[]>([]);
  priorityStats = signal<PriorityStats[]>([]);
  isLoading = signal(true);

  // Computed values for UI
  totalSentDisplay = computed(() => this.formatNumber(this.metrics().totalSent));
  deliveredDisplay = computed(() => this.formatNumber(this.metrics().delivered));
  failedDisplay = computed(() => this.formatNumber(this.metrics().failed));
  inQueueDisplay = computed(() => this.formatNumber(this.metrics().inQueue));
  successRateDisplay = computed(() => `${this.metrics().successRate.toFixed(1)}%`);
  avgTimeDisplay = computed(() => `${this.metrics().avgDeliveryTime.toFixed(2)}s`);

  // Channel data for bar chart
  channelData = computed(() => [
    { label: 'Email', value: this.getChannelValue('EMAIL'), color: '#D85A30' },
    { label: 'SMS', value: this.getChannelValue('SMS'), color: '#BA7517' },
    { label: 'In-App', value: this.getChannelValue('IN_APP'), color: '#639922' }
  ]);

  maxChannel = computed(() => Math.max(...this.channelData().map(d => d.value), 1));

  // Priority data for bar chart
  priorityData = computed(() => [
    { label: 'High', value: this.getPriorityValue('HIGH'), color: '#D85A30' },
    { label: 'Medium', value: this.getPriorityValue('MEDIUM'), color: '#BA7517' },
    { label: 'Low', value: this.getPriorityValue('LOW'), color: '#639922' }
  ]);

  maxPriority = computed(() => Math.max(...this.priorityData().map(d => d.value), 1));

  constructor() {
    this.loadMetrics();
    this.loadChannelStats();
    this.loadPriorityStats();
    // Poll every 15 seconds for real-time updates
    const subscription = interval(15000).pipe(
      startWith(0),
      switchMap(() => {
        this.loadChannelStats();
        this.loadPriorityStats();
        return this.fetchMetrics();
      })
    ).subscribe();

    // Clean up subscription when component is destroyed
    this.destroyRef.onDestroy(() => {
      subscription.unsubscribe();
    });
  }

  private loadMetrics(): void {
    this.fetchMetrics().subscribe({
      next: (data) => {
        this.metrics.set(data);
        this.isLoading.set(false);
      }
    });
  }

  private fetchMetrics() {
    return this.http.get<DeliveryMetrics>(this.API_URL).pipe(
      catchError((error) => {
        console.error('Failed to fetch delivery metrics:', error);
        // Use mock data if API fails
        this.metrics.set(this.getMockMetrics());
        this.channelStats.set(this.getMockChannelStats());
        this.priorityStats.set(this.getMockPriorityStats());
        this.isLoading.set(false);
        return of(this.getMockMetrics());
      })
    );
  }

  private loadChannelStats(): void {
    this.http.get<ChannelStats[]>('http://localhost:8080/api/delivery/metrics/channel').pipe(
      catchError((error) => {
        console.error('Failed to fetch channel stats:', error);
        this.channelStats.set(this.getMockChannelStats());
        return of(this.getMockChannelStats());
      })
    ).subscribe(stats => {
      this.channelStats.set(stats);
    });
  }

  private loadPriorityStats(): void {
    this.http.get<PriorityStats[]>('http://localhost:8080/api/delivery/metrics/priority').pipe(
      catchError((error) => {
        console.error('Failed to fetch priority stats:', error);
        this.priorityStats.set(this.getMockPriorityStats());
        return of(this.getMockPriorityStats());
      })
    ).subscribe(stats => {
      this.priorityStats.set(stats);
    });
  }

  private getChannelValue(channel: 'EMAIL' | 'SMS' | 'IN_APP'): number {
    const stat = this.channelStats().find(s => s.channel === channel);
    return stat?.delivered || 0;
  }

  private getPriorityValue(priority: 'HIGH' | 'MEDIUM' | 'LOW'): number {
    const stat = this.priorityStats().find(s => s.priority === priority);
    return stat?.delivered || 0;
  }

  private formatNumber(num: number): string {
    if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  }

  private getMockMetrics(): DeliveryMetrics {
    return {
      totalSent: 2418,
      delivered: 2301,
      failed: 62,
      inQueue: 55,
      avgDeliveryTime: 1.2,
      successRate: 95.2
    };
  }

  private getMockChannelStats(): ChannelStats[] {
    return [
      { channel: 'EMAIL', sent: 1200, delivered: 1150, failed: 50, avgTime: 1.5 },
      { channel: 'SMS', sent: 800, delivered: 780, failed: 20, avgTime: 0.8 },
      { channel: 'IN_APP', sent: 418, delivered: 371, failed: 47, avgTime: 0.3 }
    ];
  }

  private getMockPriorityStats(): PriorityStats[] {
    return [
      { priority: 'HIGH', sent: 500, delivered: 490, failed: 10 },
      { priority: 'MEDIUM', sent: 1200, delivered: 1140, failed: 60 },
      { priority: 'LOW', sent: 718, delivered: 671, failed: 47 }
    ];
  }
}
