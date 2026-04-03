import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export default class Sidebar {
// We can eventually drive this from a JSON config if the app grows!
  navItems = [
    { path: '/send', label: 'Send', icon: 'send' },
    { path: '/delivery-status', label: 'Delivery status', icon: 'bar_chart' },
    { path: '/queue', label: 'Queue stats', icon: 'reorder' },
    { path: '/failed', label: 'Failed', icon: 'error_outline' }
  ];
}
