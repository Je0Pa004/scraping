import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css']
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  sidebarCollapsed = false;
  currentRoute = '';
  private destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    // Track current route for conditional sidebar display
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe((event: NavigationEnd) => {
        this.currentRoute = event.url;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  shouldShowSidebar(): boolean {
    // Hide sidebar on auth pages
    const authRoutes = ['/auth/login', '/auth/register'];
    return !authRoutes.includes(this.currentRoute);
  }

  shouldShowNavbar(): boolean {
    // Show navbar on all pages except auth pages
    const authRoutes = ['/auth/login', '/auth/register'];
    return !authRoutes.includes(this.currentRoute);
  }

  getCurrentPageTitle(): string {
    const routeTitles: { [key: string]: string } = {
      '/user/dashboard': 'Dashboard',
      '/user/search': 'Search Profiles',
      '/user/results': 'Search Results',
      '/user/subscription': 'Subscription',
      '/user/profile': 'Profile',
      '/admin/dashboard': 'Admin Dashboard',
      '/admin/users': 'User Management',
      '/admin/subscriptions': 'Subscription Management',
      '/admin/logs': 'System Logs'
    };

    return routeTitles[this.currentRoute] || 'Recruitment Platform';
  }

  get notifications$() {
    return this.notificationService.notifications$;
  }

  removeNotification(id: string): void {
    this.notificationService.removeNotification(id);
  }
}
