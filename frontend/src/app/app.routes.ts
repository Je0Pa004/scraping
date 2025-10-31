import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { AdminGuard } from './core/guards/admin.guard';
import { SubscriptionGuard } from './core/guards/subscription.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'welcome', pathMatch: 'full' },
  { path: 'welcome', loadComponent: () => import('./components/landing/welcome/welcome.component').then(m => m.WelcomeComponent) },
  { path: 'login', loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent) },
  { path: 'admin/login', loadComponent: () => import('./components/admin-login/admin-login.component').then(m => m.AdminLoginComponent) },
  { path: 'register', loadComponent: () => import('./components/register/register.component').then(m => m.RegisterComponent) },
  { path: 'dashboard', redirectTo: 'app/dashboard', pathMatch: 'full' },
  { path: 'app', canActivate: [AuthGuard], loadComponent: () => import('./components/layout/app-layout.component').then(m => m.AppLayoutComponent), children: [
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    // Dashboard accessible without active subscription
    { path: 'dashboard', loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent) },
    { path: 'search', canActivate: [SubscriptionGuard], loadComponent: () => import('./components/pages/search/search.component').then(m => m.SearchComponent) },
    { path: 'results', canActivate: [SubscriptionGuard], loadComponent: () => import('./components/pages/results/results.component').then(m => m.ResultsComponent) },
    { path: 'profile-detail/:id', canActivate: [SubscriptionGuard], loadComponent: () => import('./components/pages/profile-detail/profile-detail.component').then(m => m.ProfileDetailComponent) },
    { path: 'leads', canActivate: [SubscriptionGuard], loadComponent: () => import('./components/pages/my-candidates/my-candidates.component').then(m => m.MyCandidatesComponent) },
    { path: 'stats', canActivate: [SubscriptionGuard], loadComponent: () => import('./components/pages/stats/stats.component').then(m => m.StatsComponent) },
    { path: 'subscriptions', loadComponent: () => import('./components/pages/subscriptions/subscriptions.component').then(m => m.SubscriptionsComponent) },
    { path: 'payment', loadComponent: () => import('./components/pages/payment/payment.component').then(m => m.PaymentComponent) },
    { path: 'profile', canActivate: [SubscriptionGuard], loadComponent: () => import('./components/pages/profile/profile.component').then(m => m.ProfileComponent) },
  ] },
  { path: 'admin', canActivate: [AuthGuard, AdminGuard], loadComponent: () => import('./components/layout/app-layout.component').then(m => m.AppLayoutComponent), children: [
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    { path: 'dashboard', loadComponent: () => import('./features/admin/components/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
    { path: 'users', loadComponent: () => import('./features/admin/components/manage-users/manage-users.component').then(m => m.ManageUsersComponent) },
    { path: 'subscriptions', loadComponent: () => import('./features/admin/components/manage-subscriptions/manage-subscriptions.component').then(m => m.ManageSubscriptionsComponent) },
    { path: 'payments', loadComponent: () => import('./components/admin/payments-management/payments-management.component').then(m => m.PaymentsManagementComponent) },
    { path: 'scraping', loadComponent: () => import('./components/admin/scraping-management/scraping-management.component').then(m => m.ScrapingManagementComponent) },
    { path: 'logs', loadComponent: () => import('./components/admin/logs-management/logs-management.component').then(m => m.LogsManagementComponent) },
  ] },
  { path: '**', redirectTo: 'welcome' }
];
