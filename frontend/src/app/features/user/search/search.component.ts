import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { ScrapingService } from '../../../core/services/scraping.service';
import { SubscriptionService } from '../../../core/services/subscription.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';

import { SearchCriteria } from '../../../core/models/search.model';
import { Subscription } from '../../../core/models/subscription.model';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit, OnDestroy {
  searchForm: FormGroup;
  isLoading = false;
  isSearching = false;
  currentSubscription: Subscription | null = null;
  searchHistory: any[] = [];

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private scrapingService: ScrapingService,
    private subscriptionService: SubscriptionService,
    private notificationService: NotificationService,
    private authService: AuthService,
    private router: Router
  ) {
    this.searchForm = this.fb.group({
      location: ['', [Validators.required, Validators.minLength(2)]],
      keywords: [''],
      experience: [''],
      education: [''],
      skills: [''],
      company: [''],
      industry: [''],
      maxResults: [50, [Validators.min(1), Validators.max(500)]],
      searchRadius: [50],
      source: ['LINKEDIN', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadUserSubscription();
    this.loadSearchHistory();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadUserSubscription(): void {
    this.subscriptionService.getUserSubscriptions()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (subscriptions: Subscription[]) => {
          this.currentSubscription = subscriptions.find(sub => sub.isActive) || null;
        },
        error: (error: any) => {
          console.error('Error loading subscription:', error);
        }
      });
  }

  private loadSearchHistory(): void {
    this.scrapingService.getScrapings()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (scrapings: any[]) => {
          this.searchHistory = scrapings
            .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
            .slice(0, 10);
        },
        error: (error: any) => {
          console.error('Error loading search history:', error);
        }
      });
  }

  onSubmit(): void {
    if (this.searchForm.valid) {
      if (!this.canPerformSearch()) {
        this.notificationService.showWarning('You have reached your search limit. Please upgrade your subscription.');
        return;
      }

      this.isSearching = true;
      const searchCriteria: SearchCriteria = this.searchForm.value;

      this.scrapingService.performScraping(searchCriteria)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            this.isSearching = false;
            this.notificationService.showSuccess('Search completed successfully!');
            // Navigate to results page with search ID
            if (response.id) {
              this.router.navigate(['/user/results'], {
                queryParams: { searchId: response.id }
              });
            }
          },
          error: (error: any) => {
            this.isSearching = false;
            this.notificationService.showError(error.error?.message || 'Search failed. Please try again.');
          }
        });
    } else {
      this.markFormGroupTouched();
    }
  }

  private canPerformSearch(): boolean {
    if (!this.currentSubscription) return false;
    // For now, allow search if subscription is active
    // TODO: Implement credit system based on backend response
    return this.currentSubscription.isActive;
  }

  onQuickSearch(location: string): void {
    this.searchForm.patchValue({ location });
    this.onSubmit();
  }

  onLoadPreviousSearch(search: any): void {
    // Load previous search criteria
    if (search.criteria) {
      this.searchForm.patchValue(search.criteria);
    }
  }

  onClearForm(): void {
    this.searchForm.reset({
      maxResults: 50,
      searchRadius: 50
    });
  }

  getCreditsRemaining(): number {
    // TODO: Implement based on backend credit system
    return 100; // Placeholder
  }

  getMaxResults(): number {
    if (!this.currentSubscription) return 50;
    // Different limits based on subscription type
    return this.currentSubscription.type?.name?.toLowerCase().includes('premium') ? 500 : 100;
  }

  private markFormGroupTouched(): void {
    Object.keys(this.searchForm.controls).forEach(key => {
      const control = this.searchForm.get(key);
      control?.markAsTouched();
    });
  }

  get location() { return this.searchForm.get('location'); }
  get keywords() { return this.searchForm.get('keywords'); }
  get maxResults() { return this.searchForm.get('maxResults'); }
}
