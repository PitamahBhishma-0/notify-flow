import { Component, computed, signal } from '@angular/core';
import { MetricCard } from '../../shared/components/metric-card/metric-card';
import { BarChart } from '../../shared/components/bar-chart/bar-chart';
@Component({
  selector: 'app-queue',
  imports: [BarChart, MetricCard],
  standalone: true,
  templateUrl: './queue.html',
  styleUrl: './queue.scss',
})
export default class Queue {
// Mock data - in production, this comes from a Service!
  priorityData = signal([
    { label: 'High', value: 1500, color: '#D85A30' },
    { label: 'Medium', value: 1000, color: '#BA7517' },
    { label: 'Low', value: 100, color: '#639922' }
  ]);

  // Find the highest value to calibrate the chart scale
  maxPriority = computed(() => Math.max(...this.priorityData().map(d => d.value)));

   messageTypeData = signal([
    { label: 'Email', value: 2000, color: '#D85A30' },
    { label: 'SMS', value: 1000, color: '#BA7517' },
    { label: 'In-App', value: 5000, color: '#639922' }
  ]);

  maxMessageType = computed(() => Math.max(...this.messageTypeData().map(d => d.value)));
}
