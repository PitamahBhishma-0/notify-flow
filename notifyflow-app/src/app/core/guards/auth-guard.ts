import { isPlatformBrowser } from '@angular/common';
import { inject ,PLATFORM_ID} from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
export const authGuard: CanActivateFn = (route, state) => {
const router = inject(Router);
  const platformId = inject(PLATFORM_ID); // Get the current platform

  // 3. Only touch localStorage if we are in the browser
  if (isPlatformBrowser(platformId)) {
    const token = localStorage.getItem('notify_token');

    if (token) {
      return true;
    }
  }

  // If we are on the server OR there is no token, redirect to login
  // Note: On the server, this will prevent the protected page from pre-rendering.
  return router.createUrlTree(['/login']);
};
