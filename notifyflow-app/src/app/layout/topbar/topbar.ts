import { Component, inject } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter, map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
@Component({
  selector: 'app-topbar',
  imports: [MatButtonModule, MatIconModule, MatMenuModule],
  templateUrl: './topbar.html',
  styleUrl: './topbar.scss',
})
export default class Topbar {
private router = inject(Router);

  // We convert the Router URL into a Signal that our template watches
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
    if (url.includes('status')) return 'Delivery status';
    if (url.includes('queue')) return 'Queue stats';
    if (url.includes('failed')) return 'Failed notifications';
    return 'Send notification';
  }

  private getSub(url: string): string {
    if (url.includes('status')) return 'Real-time delivery log';
    // ... add other cases
    return 'Compose and dispatch a new notification';
  }

  logout() {
  localStorage.removeItem('notify_token');
  this.router.navigate(['/login']);
}
}
