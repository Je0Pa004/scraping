import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { SubscriptionService } from '../../core/services/subscription.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  isGuest = false;
  hasActiveSubscription = false;

  constructor(private auth: AuthService, private subs: SubscriptionService, private router: Router) {
    this.auth.currentUser$.subscribe(u => {
      const raw: any = (u as any)?.roles ?? [];
      const list: string[] = Array.isArray(raw) ? raw : (typeof raw === 'string' ? raw.split(',') : []);
      const normalized = list.map(r => String(r).trim().toUpperCase());
      this.isGuest = normalized.includes('ROLE_GUEST');

      if (u) {
        this.subs.getUserSubscriptions().subscribe({
          next: (arr: any[]) => {
            const now = Date.now();
            this.hasActiveSubscription = (arr || []).some(s => {
              const ok = !!(s as any).statut;
              const end = (s as any).dateFin ? Date.parse((s as any).dateFin) : 0;
              return ok && (!end || end >= now);
            });
          },
          error: () => this.hasActiveSubscription = false
        });
      } else {
        this.hasActiveSubscription = false;
      }
    });
  }

  onStartSearch(event: MouseEvent) {
    if (this.isGuest || !this.hasActiveSubscription) {
      event.preventDefault();
      event.stopPropagation();
      this.router.navigate(['/app/subscriptions']);
      return;
    }
    this.router.navigate(['/app/search']);
  }

  onViewResults(event: MouseEvent) {
    if (this.isGuest || !this.hasActiveSubscription) {
      event.preventDefault();
      event.stopPropagation();
      this.router.navigate(['/app/subscriptions']);
      return;
    }
    this.router.navigate(['/app/results']);
  }
}
