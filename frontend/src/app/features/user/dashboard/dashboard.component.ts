import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { SubscriptionService } from '../../../core/services/subscription.service';
import { ScrapingService } from '../../../core/services/scraping.service';
import { NotificationService } from '../../../core/services/notification.service';

import { User } from '../../../core/models/user.model';
import { Subscription } from '../../../core/models/subscription.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  userSubscription: Subscription | null = null;
  recentSearches: any[] = [];
  isLoading = false;
  stats = {
    totalSearches: 0,
    activeSubscription: false,
    subscriptionExpiry: null as Date | null,
    creditsRemaining: 0
  };

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private subscriptionService: SubscriptionService,
    private scrapingService: ScrapingService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadUserData();
    this.loadDashboardStats();
    this.loadRecentSearches();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadUserData(): void {
    this.currentUser = this.authService.getCurrentUser();
  }

  private loadDashboardStats(): void {
    this.isLoading = true;

    // Load subscription info
    this.subscriptionService.getUserSubscriptions()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (subscriptions: Subscription[]) => {
          // Get the most recent active subscription
          const activeSubscription = subscriptions.find(sub => sub.statut);
          this.userSubscription = activeSubscription || null;
          this.stats.activeSubscription = activeSubscription?.statut || false;
          this.stats.subscriptionExpiry = activeSubscription ? new Date(activeSubscription.dateFin) : null;
          this.stats.creditsRemaining = activeSubscription ? (activeSubscription.typeAbonnement?.nombreScrapingMax - activeSubscription.nombreScraping) : 0;
        },
        error: (error: any) => {
          console.error('Error loading subscription:', error);
        }
      });

    // Load search statistics - using scraping service to get user's scrapings
    this.scrapingService.getScrapings()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (scrapings: any[]) => {
          this.stats.totalSearches = scrapings.length;
          this.isLoading = false;
        },
        error: (error: any) => {
          console.error('Error loading user stats:', error);
          this.isLoading = false;
        }
      });
  }

  private loadRecentSearches(): void {
    this.scrapingService.getScrapings()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (scrapings: any[]) => {
          // Get the 5 most recent searches
          this.recentSearches = scrapings
            .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
            .slice(0, 5);
        },
        error: (error: any) => {
          console.error('Error loading recent searches:', error);
        }
      });
  }

  onNewSearch(): void {
    // Navigate to search page
    // This will be handled by routerLink in template
  }

  onViewResults(searchId: string): void {
    // Navigate to results page with search ID
    // This will be handled by routerLink in template
  }

  onUpgradeSubscription(): void {
    // Navigate to subscription page
    // This will be handled by routerLink in template
  }

  getSubscriptionStatus(): string {
    if (!this.stats.activeSubscription) {
      return 'No Active Subscription';
    }

    if (this.stats.subscriptionExpiry) {
      const daysUntilExpiry = Math.ceil(
        (this.stats.subscriptionExpiry.getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24)
      );

      if (daysUntilExpiry <= 0) {
        return 'Expired';
      } else if (daysUntilExpiry <= 7) {
        return `Expires in ${daysUntilExpiry} day${daysUntilExpiry > 1 ? 's' : ''}`;
      }
    }

    return 'Active';
  }

  getSubscriptionStatusClass(): string {
    if (!this.stats.activeSubscription) {
      return 'status-inactive';
    }

    if (this.stats.subscriptionExpiry) {
      const daysUntilExpiry = Math.ceil(
        (this.stats.subscriptionExpiry.getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24)
      );

      if (daysUntilExpiry <= 0) {
        return 'status-expired';
      } else if (daysUntilExpiry <= 7) {
        return 'status-warning';
      }
    }

    return 'status-active';
  }
}
