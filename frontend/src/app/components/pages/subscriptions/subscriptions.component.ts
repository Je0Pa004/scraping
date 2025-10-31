import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SubscriptionService } from '../../../core/services/subscription.service';
import { SubscriptionType } from '../../../core/models/subscription.model';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-subscriptions',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  template: `
    <div class="p-6 max-w-7xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-black text-gray-900 mb-2">👑 {{ 'subscriptions.title' | translate }}</h1>
        <p class="text-gray-600">{{ 'subscriptions.description' | translate }}</p>
      </div>

      <!-- Current Usage -->
      <div class="bg-white rounded-2xl shadow-soft p-8 mb-8">
        <h2 class="text-xl font-bold text-gray-900 mb-6">{{ 'subscriptions.current_usage' | translate }}</h2>
        <div class="flex items-center justify-between mb-4">
          <div>
            <p class="text-sm text-gray-600 mb-1">{{ 'subscriptions.current_plan' | translate }}</p>
            <p class="text-2xl font-bold text-gray-900">{{ currentUsage }} / {{ currentLimit }} {{ 'subscriptions.searches_used' | translate }}</p>
          </div>
          <button class="px-4 py-2 text-red-600 font-semibold hover:bg-red-50 rounded-lg transition-colors duration-200">
            {{ 'subscriptions.cancel_subscription' | translate }}
          </button>
        </div>
        <div class="w-full bg-gray-200 rounded-full h-3 mb-2">
          <div class="bg-gradient-to-r from-primary-600 to-secondary-600 h-3 rounded-full" [style.width.%]="usagePercentage"></div>
        </div>
        <p class="text-sm text-gray-600">{{ 'subscriptions.renewal_date' | translate }} {{ renewalDate }}</p>
      </div>

      <!-- Plans -->
      <h2 class="text-2xl font-bold text-gray-900 mb-6">{{ 'subscriptions.choose_plan' | translate }}</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div *ngFor="let subscription of subscriptionTypes; let i = index"
             class="subscription-card bg-white rounded-2xl shadow-soft p-8 border-2"
             [class.current-plan]="isCurrent(subscription)"
             [class.border-primary-600]="isCurrent(subscription)"
             [class.border-gray-200]="!isCurrent(subscription)">

          <div *ngIf="isCurrent(subscription)" class="absolute -top-4 left-1/2 transform -translate-x-1/2 px-4 py-1 bg-primary-600 text-white text-sm font-bold rounded-full">
            {{ 'subscriptions.current_plan' | translate }}
          </div>

          <div class="flex items-center gap-2 mb-4">
            <i [class]="'fa-solid ' + getIconClass(subscription.nom) + ' text-xl'"
               [class.text-primary-600]="isCurrent(subscription)"
               [class.text-gray-600]="!isCurrent(subscription)"
               [class.text-amber-500]="i === 2"></i>
            <h3 class="text-2xl font-bold text-gray-900">{{ subscription.nom }}</h3>
          </div>

          <div class="mb-6">
            <span class="text-4xl font-black text-gray-900">{{ subscription.cout }} FCFA</span>
            <span class="text-gray-600">/{{ subscription.duree }} jours</span>
          </div>

          <p class="text-gray-600 mb-6">{{ subscription.description }}</p>

          <ul class="space-y-3 mb-8">
            <li class="flex items-start gap-2">
              <i class="fa-solid fa-check text-green-600 mt-1"></i>
              <span class="text-gray-700">{{ subscription.nombreScrapingMax }} {{ 'subscriptions.searches_per_month' | translate }}</span>
            </li>
            <li class="flex items-start gap-2">
              <i class="fa-solid fa-check text-green-600 mt-1"></i>
              <span class="text-gray-700">{{ subscription.duree }} jours de validité</span>
            </li>
            <li class="flex items-start gap-2">
              <i class="fa-solid fa-check text-green-600 mt-1"></i>
              <span class="text-gray-700">Type: {{ subscription.type }}</span>
            </li>
          </ul>

          <button *ngIf="isCurrent(subscription)"
                  class="w-full py-3 bg-gradient-to-r from-primary-600 to-secondary-600 text-white font-bold rounded-xl hover:shadow-lg transition-all duration-200">
            {{ 'subscriptions.current_plan' | translate }}
          </button>

          <button *ngIf="!isCurrent(subscription)"
                  (click)="selectSubscription(subscription)"
                  class="w-full py-3 border-2 border-gray-300 text-gray-700 font-semibold rounded-xl hover:bg-gray-50 transition-colors duration-200">
            {{ 'subscriptions.choose_plan' | translate }}
          </button>
        </div>
      </div>

      <!-- FAQ -->
      <div class="bg-gray-50 rounded-2xl p-8">
        <h2 class="text-2xl font-bold text-gray-900 mb-6">{{ 'subscriptions.faq' | translate }}</h2>
        <div class="space-y-4">
          <div>
            <h3 class="font-bold text-gray-900 mb-2">{{ 'subscriptions.faq_question_1' | translate }}</h3>
            <p class="text-gray-600">{{ 'subscriptions.faq_answer_1' | translate }}</p>
          </div>
          <div>
            <h3 class="font-bold text-gray-900 mb-2">{{ 'subscriptions.faq_question_2' | translate }}</h3>
            <p class="text-gray-600">{{ 'subscriptions.faq_answer_2' | translate }}</p>
          </div>
          <div>
            <h3 class="font-bold text-gray-900 mb-2">{{ 'subscriptions.faq_question_3' | translate }}</h3>
            <p class="text-gray-600">{{ 'subscriptions.faq_answer_3' | translate }}</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .subscription-card {
      position: relative;
    }

    .current-plan {
      box-shadow: 0 10px 25px rgba(0, 123, 255, 0.15);
    }
  `]
})
export class SubscriptionsComponent implements OnInit {
  subscriptionTypes: SubscriptionType[] = [];
  currentUsage = 7;
  currentLimit = 10;
  usagePercentage = 70;
  renewalDate = '15/02/2025';
  currentTypeId: number | null = null;

  constructor(
    private subscriptionService: SubscriptionService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadSubscriptionTypes();
    this.loadUserCurrentSubscription();
  }

  loadSubscriptionTypes() {
    this.subscriptionService.getSubscriptionTypes().subscribe({
      next: (data) => {
        // Filter only active subscriptions
        this.subscriptionTypes = data.filter(sub => sub.estActif);
      },
      error: (error) => {
        console.error('Erreur lors du chargement des abonnements:', error);
      }
    });
  }

  private loadUserCurrentSubscription() {
    this.subscriptionService.getUserSubscriptions().subscribe({
      next: (subs) => {
        const now = new Date();
        const active = (subs || []).find(s => {
          try {
            const end = s?.dateFin ? new Date(s.dateFin) : null;
            return s?.statut && (!end || end > now);
          } catch { return s?.statut; }
        });
        this.currentTypeId = active?.typeAbonnement?.id ?? null;
        if (active?.typeAbonnement) {
          this.currentLimit = active.typeAbonnement.nombreScrapingMax ?? this.currentLimit;
          this.currentUsage = active.nombreScraping ?? this.currentUsage;
          this.usagePercentage = this.currentLimit ? Math.min(100, (this.currentUsage * 100) / this.currentLimit) : 0;
          this.renewalDate = active.dateFin ? new Date(active.dateFin).toLocaleDateString() : this.renewalDate;
        }
      },
      error: (err) => {
        console.error('Erreur récupération abonnement utilisateur:', err);
      }
    });
  }

  isCurrent(subscription: SubscriptionType): boolean {
    return !!this.currentTypeId && subscription?.id === this.currentTypeId;
  }

  selectSubscription(subscription: SubscriptionType) {
    // Navigate to payment page with subscription data
    this.router.navigate(['/app/payment'], {
      state: { subscription }
    });
  }

  getIconClass(name: string): string {
    switch ((name || '').toLowerCase()) {
      case 'free':
        return 'fa-bolt';
      case 'basic':
        return 'fa-arrow-trend-up';
      case 'premium':
        return 'fa-crown';
      default:
        return 'fa-star';
    }
  }
}
