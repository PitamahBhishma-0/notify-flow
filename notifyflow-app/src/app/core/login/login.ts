import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router,RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

// Material
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { LoginRequest } from '../models/auth.model';
import { Auth } from '../services/auth';

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    RouterLink
],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export default class Login {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  errorMessage = signal<string | null>(null);
  private authService = inject(Auth);
  isLoading = signal(false);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  hidePassword = true;

  onSubmit() {
    if (this.loginForm.valid) {
      
      this.isLoading.set(true);
      this.errorMessage.set(null);

      
      const credentials = this.loginForm.value as LoginRequest;
    // if (credentials.email === 'h@h.com') {
    //   console.log('Test user detected. Bypassing login...');
    //   localStorage.setItem('notify_token', "dgsudhjshdsj");
    //   this.router.navigate(['/send']);
    //   return;
    // }
      this.authService.login(credentials).subscribe({
        next: (res) => {
          console.log('Login Successful, token stored.');
          this.router.navigate(['/send']);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set('Invalid email or password. Please try again.');
          console.error('Auth Error:', err);
        }
      });
    }
  }
}
