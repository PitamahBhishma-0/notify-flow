import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-bar-chart',
  imports: [],
  standalone: true,
  templateUrl: './bar-chart.html',
  styleUrl: './bar-chart.scss',
})
export  class BarChart {
label = input.required<string>();
  value = input.required<number>();
  maxValue = input.required<number>(); // To calculate the relative width
  color = input<string>('var(--brand-primary)');

  // Signal-based calculation for the width
  percentage = computed(() => {
    if (this.maxValue() === 0) return 0;
    return (this.value() / this.maxValue()) * 100;
  });
}
