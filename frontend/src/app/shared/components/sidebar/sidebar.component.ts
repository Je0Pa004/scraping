import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models/user.model';
import { SubscriptionService } from '../../../core/services/subscription.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  currentUser: User | null = null;
  isLoggedIn = false;
  hasActiveSubscription = false;
  isSimpleUser = false;
  isGuest = false;

  menuItems = [
    {
      label: 'Dashboard',
      icon: 'fas fa-tachometer-alt',
      route: '/app/dashboard',
      roles: ['GUEST', 'USER', 'ADMIN']
    },
    {
      label: 'Recherche',
      icon: 'fas fa-search',
      route: '/app/search',
      roles: ['GUEST', 'USER', 'ADMIN']
    },
    {
      label: 'Résultats',
      icon: 'fas fa-list',
      route: '/app/results',
      roles: ['GUEST', 'USER', 'ADMIN']
    },
    {
      label: 'Mes candidats',
      icon: 'fas fa-users',
      route: '/app/leads',
      roles: ['GUEST', 'USER', 'ADMIN']
    },
    {
      label: 'Statistiques',
      icon: 'fas fa-chart-line',
      route: '/app/stats',
      roles: ['GUEST', 'USER', 'ADMIN']
    },
    {
      label: 'Offres',
      icon: 'fas fa-crown',
      route: '/app/subscriptions',
      roles: ['GUEST', 'USER', 'ADMIN']
    },
    {
      label: 'Profil',
      icon: 'fas fa-user',
      route: '/app/profile',
      roles: ['GUEST', 'USER', 'ADMIN']
    },
    {
      label: 'Admin Dashboard',
      icon: 'fas fa-cog',
      route: '/admin/dashboard',
      roles: ['ADMIN']
    },
    {
      label: 'Manage Users',
      icon: 'fas fa-users',
      route: '/admin/users',
      roles: ['ADMIN']
    },
    {
      label: 'Manage Subscriptions',
      icon: 'fas fa-tags',
      route: '/admin/subscriptions',
      roles: ['ADMIN']
    },
    {
      label: 'System Logs',
      icon: 'fas fa-file-alt',
      route: '/admin/logs',
      roles: ['ADMIN']
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
    private subscriptionService: SubscriptionService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isLoggedIn = !!user;
      this.isSimpleUser = (user as any)?.typeCompte?.toString().toUpperCase() === 'USER';
      // Derive ROLE_GUEST from roles
      const raw = (user as any)?.roles ?? [];
      const list: string[] = Array.isArray(raw) ? raw : (typeof raw === 'string' ? raw.split(',') : []);
      const normalized = list.map(r => String(r).trim().toUpperCase());
      this.isGuest = normalized.includes('ROLE_GUEST');
      // Load subscription status when user changes
      if (this.isLoggedIn) {
        this.subscriptionService.getUserSubscriptions().subscribe({
          next: (list: any[]) => {
            const now = Date.now();
            this.hasActiveSubscription = (list || []).some(s => {
              const statusOk = !!(s as any).statut;
              const end = (s as any).dateFin ? Date.parse((s as any).dateFin) : 0;
              return statusOk && (!end || end >= now);
            });
          },
          error: () => {
            this.hasActiveSubscription = false;
          }
        });
      } else {
        this.hasActiveSubscription = false;
      }
    });
  }

  hasRole(requiredRoles: string[]): boolean {
    const anyUser: any = this.currentUser as any;
    const rawRoles: any = anyUser?.roles ?? anyUser?.role ?? [];
    const list: string[] = Array.isArray(rawRoles) ? rawRoles : (typeof rawRoles === 'string' ? rawRoles.split(',') : []);
    if (!list.length) return false;
    const normalized = list.map(r => String(r).trim().toUpperCase()).filter(Boolean);
    const withPrefix = new Set(normalized.map(r => r.startsWith('ROLE_') ? r : `ROLE_${r}`));

    return requiredRoles.some(req => {
      const want = String(req).trim().toUpperCase();
      const wantPref = want.startsWith('ROLE_') ? want : `ROLE_${want}`;
      return withPrefix.has(wantPref);
    });
  }

  isActive(route: string): boolean {
    return this.router.url === route;
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  isDisabled(item: { route: string }): boolean {
    // Disable premium features for guest users (or no active subscription)
    const premiumRoutes = new Set<string>(['/app/search', '/app/results', '/app/leads', '/app/stats']);
    const shouldDisable = (this.isGuest || !this.hasActiveSubscription) && premiumRoutes.has(item.route);
    return shouldDisable;
  }

  onClick(item: { route: string }, event: MouseEvent): void {
    if (this.isDisabled(item)) {
      event.preventDefault();
      event.stopPropagation();
      return;
    }
    this.navigateTo(item.route);
  }
}
