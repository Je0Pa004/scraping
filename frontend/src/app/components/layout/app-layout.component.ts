import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { ActivatedRoute, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { SubscriptionService } from '../../core/services/subscription.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './app-layout.component.html',
  styleUrls: ['./app-layout.component.css']
})
export class AppLayoutComponent {
  isDarkMode = false;
  hideSidebar = false;
  isGuest = false;
  hasActiveSubscription = false;

  constructor(public auth: AuthService, public router: Router, private route: ActivatedRoute, private subscriptionService: SubscriptionService) {
    // Check if dark mode was previously enabled
    this.isDarkMode = localStorage.getItem('darkMode') === 'true';
    if (this.isDarkMode) {
      document.documentElement.classList.add('dark');
    }

    this.updateHideSidebar();
    this.router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe(() => {
      this.updateHideSidebar();
      this.refreshSubscriptionStatus();
    });
  }

  ngOnInit(): void {
    this.auth.currentUser$.subscribe(u => {
      const raw: any = (u as any)?.roles ?? [];
      const list: string[] = Array.isArray(raw) ? raw : (typeof raw === 'string' ? raw.split(',') : []);
      const normalized = list.map(r => String(r).trim().toUpperCase());
      this.isGuest = normalized.includes('ROLE_GUEST');
      this.refreshSubscriptionStatus();
    });

    // Listen to subscription changes to update UI immediately after payment
    this.subscriptionService.changes$.subscribe(() => this.refreshSubscriptionStatus());
  }

  get isAdminRoute(): boolean {
    const url = this.router.url || '';
    return url.startsWith('/admin');
  }

  get isAdmin(): boolean {
    const u: any = this.auth.getCurrentUser();
    const raw: any = u?.roles ?? u?.role ?? [];
    const list: string[] = Array.isArray(raw) ? raw : (typeof raw === 'string' ? raw.split(',') : []);
    const normalized = list.map(r => String(r).trim().toUpperCase());
    return normalized.includes('ROLE_ADMIN') || normalized.includes('ADMIN');
  }

  private updateHideSidebar() {
    const url = this.router.url || '';
    const dataHide = !!this.getDeepestData('hideChrome');
    // Keep chrome visible; only hide when a child route explicitly asks via data.hideChrome
    this.hideSidebar = !this.isAdminRoute && dataHide;
  }

  private getDeepestData(key: string): any {
    let r = this.route.firstChild;
    let data: any = null;
    while (r) {
      data = r.snapshot?.data?.[key] ?? data;
      r = r.firstChild as any;
    }
    return data;
  }

  toggleDarkMode() {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('darkMode', 'true');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('darkMode', 'false');
    }
  }

  logout() {
    this.auth.logout();
  }

  isDisabled(route: string): boolean {
    const premium = new Set<string>(['/app/search','/app/results','/app/leads','/app/stats']);
    return !this.hasActiveSubscription && premium.has(route);
  }

  onNavClick(event: MouseEvent, route: string) {
    if (this.isDisabled(route)) {
      event.preventDefault();
      event.stopPropagation();
      return;
    }
  }

  private refreshSubscriptionStatus(): void {
    const u = this.auth.getCurrentUser();
    if (!u) {
      this.hasActiveSubscription = false;
      return;
    }
    this.subscriptionService.getUserSubscriptions().subscribe({
      next: (subs: any[]) => {
        const now = Date.now();
        this.hasActiveSubscription = (subs || []).some(s => {
          const ok = !!(s as any).statut;
          const end = (s as any).dateFin ? Date.parse((s as any).dateFin) : 0;
          return ok && (!end || end >= now);
        });
      },
      error: () => this.hasActiveSubscription = false
    });
  }
}
