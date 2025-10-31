import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { SubscriptionService } from '../../../core/services/subscription.service';

import { User } from '../../../core/models/user.model';
import { Subscription } from '../../../core/models/subscription.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  userSubscription: Subscription | null = null;

  // Form data
  companyName: string = '';
  email: string = '';
  sector: string = '';
  companySize: string = '';
  fullName: string = '';
  phone: string = '';
  memberSince: string = '';
  currentPlan: string = 'Free Plan';

  // Password change
  currentPassword: string = '';
  newPassword: string = '';
  confirmPassword: string = '';

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private subscriptionService: SubscriptionService
  ) {}

  ngOnInit(): void {
    this.loadUserData();
    this.loadSubscriptionData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadUserData(): void {
    this.currentUser = this.authService.getCurrentUser();
    if (this.currentUser) {
      // Populate form fields from user data
      this.email = this.currentUser.email || '';
      this.fullName = (this.currentUser as any).nom || (this.currentUser as any).nomComplet || this.currentUser.username || '';

      // Format member since date
      if (this.currentUser.createdAt) {
        const date = new Date(this.currentUser.createdAt);
        this.memberSince = date.toLocaleDateString('fr-FR', {
          day: 'numeric',
          month: 'long',
          year: 'numeric'
        });
      }
    }
  }

  private loadSubscriptionData(): void {
    this.subscriptionService.getUserSubscriptions()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (subscriptions: Subscription[]) => {
          // Get the most recent active subscription
          const activeSubscription = subscriptions.find(sub => sub.statut);
          this.userSubscription = activeSubscription || null;

          // Update current plan display
          if (activeSubscription && activeSubscription.typeAbonnement) {
            this.currentPlan = activeSubscription.typeAbonnement.nom || 'Premium Plan';
          } else if (activeSubscription) {
            this.currentPlan = 'Plan sans détails';
          } else {
            this.currentPlan = 'Free Plan';
          }
        },
        error: (error: any) => {
          console.error('Error loading subscription:', error);
          this.currentPlan = 'Free Plan';
        }
      });
  }

  getInitials(): string {
    if (!this.fullName && !this.companyName && !this.email) return '??';
    const name = this.fullName || this.companyName || this.email;
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  hasValidSubscription(): boolean {
    return !!(this.userSubscription && this.userSubscription.typeAbonnement);
  }

  hasSubscriptionWithDetails(): boolean {
    return !!(this.userSubscription && this.userSubscription.typeAbonnement && this.userSubscription.dateFin);
  }

  getRemainingSearches(): number {
    if (this.userSubscription && this.userSubscription.typeAbonnement) {
      return (this.userSubscription.typeAbonnement.nombreScrapingMax - (this.userSubscription.nombreScraping || 0)) || 0;
    }
    return 0;
  }

  getUsedSearches(): number {
    return this.userSubscription?.nombreScraping || 0;
  }

  getMaxSearches(): number {
    return this.userSubscription?.typeAbonnement?.nombreScrapingMax || 0;
  }

  hasUsageInfo(): boolean {
    return !!(this.userSubscription?.typeAbonnement?.nombreScrapingMax);
  }

  getUsagePercentage(): number {
    if (this.hasUsageInfo() && this.userSubscription) {
      return ((this.getUsedSearches() / this.getMaxSearches()) * 100) || 0;
    }
    return 0;
  }

  getFormattedExpiryDate(): string | null {
    if (this.userSubscription?.dateFin) {
      return new Date(this.userSubscription.dateFin).toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    }
    return null;
  }

  saveGeneralInfo(): void {
    console.log('Saving general info:', {
      companyName: this.companyName,
      email: this.email,
      sector: this.sector,
      companySize: this.companySize
    });
    alert('Informations générales enregistrées avec succès!');
  }

  saveUserInfo(): void {
    console.log('Saving user info:', {
      fullName: this.fullName,
      phone: this.phone
    });
    alert('Informations utilisateur enregistrées avec succès!');
  }

  changePassword(): void {
    if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
      alert('Veuillez remplir tous les champs');
      return;
    }
    
    if (this.newPassword !== this.confirmPassword) {
      alert('Les mots de passe ne correspondent pas');
      return;
    }
    
    if (this.newPassword.length < 6) {
      alert('Le mot de passe doit contenir au moins 6 caractères');
      return;
    }
    
    console.log('Changing password');
    alert('Mot de passe changé avec succès!');
    
    // Reset password fields
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
  }

  logout(): void {
    this.authService.logout();
  }
}
