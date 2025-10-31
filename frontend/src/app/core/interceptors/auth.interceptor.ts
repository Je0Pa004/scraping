import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError, switchMap, of } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Clone request and add authorization header if token exists
  const isAuthRequest = req.url.includes('/login') || req.url.includes('/register') || req.url.includes('/refresh') || req.url.includes('/forgot-password');
  if (token && !isAuthRequest) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  const alreadyRetried = req.headers.has('X-Retry-After-Refresh');

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const hadToken = !!token;
      const isAuthEndpoint = req.url.includes('/login') || req.url.includes('/register') || req.url.includes('/refresh') || req.url.includes('/forgot-password');
      if (hadToken && error.status === 401 && !isAuthEndpoint && !alreadyRetried) {
        // Try refresh token once
        const refreshToken = authService.getRefreshToken();
        if (refreshToken) {
          return authService.refreshToken().pipe(
            switchMap(() => {
              const newToken = authService.getToken();
              const retriedReq = req.clone({
                setHeaders: newToken ? { Authorization: `Bearer ${newToken}`, 'X-Retry-After-Refresh': '1' } : { 'X-Retry-After-Refresh': '1' }
              });
              return next(retriedReq);
            }),
            catchError((refreshErr) => {
              authService.logout();
              return throwError(() => refreshErr);
            })
          );
        }
        // No refresh token -> logout
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};
