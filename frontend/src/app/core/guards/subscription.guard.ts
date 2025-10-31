import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { SubscriptionService } from '../services/subscription.service';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class SubscriptionGuard implements CanActivate {
  constructor(
    private subscriptionService: SubscriptionService,
    private auth: AuthService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean | UrlTree> {
    if (!this.auth.isAuthenticated()) {
      return of(this.router.createUrlTree(['/login']));
    }

    // Allow admin directly
    if (this.auth.hasRole('ADMIN')) {
      return of(true);
    }

    // For all non-admin users, require an active subscription
    return this.subscriptionService.getUserSubscriptions().pipe(
      map(list => {
        const now = Date.now();
        const hasActive = (list || []).some(s => {
          const statusOk = !!(s as any).statut;
          const end = (s as any).dateFin ? Date.parse((s as any).dateFin) : 0;
          return statusOk && (!end || end >= now);
        });
        return hasActive ? true : this.router.createUrlTree(['/app/subscriptions']);
      }),
      catchError(() => of(this.router.createUrlTree(['/app/subscriptions'])))
    );
  }
}
