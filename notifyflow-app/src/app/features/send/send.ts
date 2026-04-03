import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

// Material Imports
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';

// Our Reusable "Dumb" Components
import { MetricCard } from '../../shared/components/metric-card/metric-card';
import { NotificationPriority, PriorityBtn } from '../../shared/components/priority-btn/priority-btn';
import { Notification } from '../../core/services/notification';
import { NotificationRequest } from '../../core/models/notification.model';

@Component({
  selector: 'app-send',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MetricCard,
    PriorityBtn
  ],
  templateUrl: './send.html',
  styleUrl: './send.scss'
})
export default class SendComponent {
  private fb = inject(FormBuilder);
  private notificationService=inject(Notification);
  isSending = signal(false);
  // Define the Form Model
    sendForm = this.fb.group({
    recipientUserId: ['', [Validators.required, Validators.minLength(5)]],
    channel: ['Email', Validators.required],
    subject: ['', Validators.required],
    body: ['', [Validators.required, Validators.maxLength(500)]],
    priority: ['med' as NotificationPriority]
  });

  // This handles the event emitted by our Dumb Priority component
  onPriorityChanged(priority: NotificationPriority) {
    this.sendForm.patchValue({ priority });
  }

  dispatch() {
  if (this.sendForm.valid) {
    this.isSending.set(true);

    // Manually map form fields to the API interface
    const formValue = this.sendForm.value;
    
    const payload: NotificationRequest = {
      recipientUserId: formValue.recipientUserId ?? '', 
      subject: formValue.subject ?? '',
      body: formValue.body ?? '',
      // Ensure the strings match your backend Enums exactly
      channel: (formValue.channel?.toUpperCase() as any) || 'EMAIL',
      priority: (formValue.priority?.toUpperCase() as any) || 'LOW'
    };

    this.notificationService.sendNotification(payload).subscribe({
      next: (res) => {
        console.log('Dispatched via Gateway:', res);
        this.isSending.set(false);
        this.sendForm.reset();
      },
      error: (err) => {
        console.error('Submission failed:', err);
        this.isSending.set(false);
      }
    });
  }
}
}