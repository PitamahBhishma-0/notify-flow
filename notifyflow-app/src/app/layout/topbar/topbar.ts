import { Component, inject, PLATFORM_ID } from '@angular/core';
import { Router, NavigationEnd, RouterLink } from '@angular/router';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { filter, map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';

// Material Imports
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';

// Custom Services
import { Monitoring } from '../../core/services/monitoring';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [
    CommonModule, 
    MatButtonModule, 
    MatIconModule, 
    MatMenuModule, 
    MatDividerModule,
    MatTooltipModule
  ],
  templateUrl: './topbar.html',
  styleUrl: './topbar.scss',
})
export default class Topbar {
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private monitoring = inject(Monitoring);

  // Link to the monitoring service signal
  health = this.monitoring.health;

  pageTitle = toSignal(
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      map(() => this.getTitle(this.router.url))
    ), { initialValue: 'Send notification' }
  );

  pageSub = toSignal(
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      map(() => this.getSub(this.router.url))
    ), { initialValue: 'Compose and dispatch a new notification' }
  );

  private getTitle(url: string): string {
    if (url.includes('status')) return 'Delivery Status';
    if (url.includes('queue')) return 'Queue Stats';
    if (url.includes('failed')) return 'Failed Notifications';
    return 'Send Notification';
  }

  private getSub(url: string): string {
    if (url.includes('status')) return 'Real-time delivery log';
    if (url.includes('queue')) return 'Monitor microservice throughput';
    return 'Compose and dispatch a new notification';
  }

  logout() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('notify_token');
    }
    this.router.navigate(['/login']);
  }
}