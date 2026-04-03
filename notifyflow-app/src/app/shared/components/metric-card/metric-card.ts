import { CommonModule } from '@angular/common';
import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-metric-card',
  imports: [CommonModule],
  standalone: true,
  templateUrl: './metric-card.html',
  styleUrl: './metric-card.scss',
})
export  class MetricCard {
// Inputs as Signals
  label = input.required<string>();
  value = input.required<string | number>();
  trend = input<string>();

  // Derived state: Automatically calculates the CSS class based on the trend string
  trendClass = computed(() => {
    const t = this.trend();
    if (!t) return '';
    if (t.includes('+')) return 'up';
    if (t.includes('-') || t.toLowerCase().includes('down')) return 'down';
    return '';
  });
}
