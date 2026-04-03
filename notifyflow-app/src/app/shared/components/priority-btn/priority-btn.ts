import { Component, output, signal } from '@angular/core';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
export type NotificationPriority = 'HIGH' | 'med' | 'LOW';
@Component({
  selector: 'app-priority-btn',
  imports: [MatButtonToggleModule],
  templateUrl: './priority-btn.html',
  styleUrl: './priority-btn.scss',
})
export class PriorityBtn {
// Internal State (Signal)
  selectedPriority = signal<NotificationPriority>('med');

  // The "Output" tells the parent when something changed
  priorityChange = output<NotificationPriority>();

  select(value: NotificationPriority) {
    this.selectedPriority.set(value);
    this.priorityChange.emit(value);
  }
}
