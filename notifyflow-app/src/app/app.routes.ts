import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./core/login/login') },
  { path: 'register', loadComponent: () => import('./core/register/register') },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell/shell'),
    children: [
      {
        path: 'send',
        loadComponent: () => import('./features/send/send')
      },
      {
        path: 'delivery-status',
        loadComponent: () => import('./features/delivery-status/delivery-status')
      },
      {
        path: 'queue',
        loadComponent: () => import('./features/queue/queue')
      },
      {
        path: 'failed',
        loadComponent: () => import('./features/failed/failed')
      },
      { path: '', redirectTo: 'send', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'send' }
];
