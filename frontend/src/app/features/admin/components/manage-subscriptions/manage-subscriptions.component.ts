import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SubscriptionType } from '../../../../core/models/subscription.model';
import { SubscriptionService } from '../../../../core/services/subscription.service';

@Component({
  selector: 'app-manage-subscriptions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './manage-subscriptions.component.html',
  styleUrl: './manage-subscriptions.component.css'
})
export class ManageSubscriptionsComponent implements OnInit {
  subscriptions: SubscriptionType[] = [];
  loading = false;
  error: string | null = null;
  showModal = false;
  showDeleteModal = false;
  showDetailsModal = false;
  isEditMode = false;
  subscriptionToDelete: SubscriptionType | null = null;
  selectedSubscription: SubscriptionType | null = null;
  
  currentSubscription: Partial<SubscriptionType> = {
    nom: '',
    description: '',
    cout: 0,
    duree: 30,
    nombreScrapingMax: 100,
    type: 'MENSUEL',
    estActif: true
  };

  constructor(private subscriptionService: SubscriptionService) {}

  ngOnInit() {
    this.loadSubscriptions();
  }

  loadSubscriptions() {
    this.loading = true;
    this.subscriptionService.getSubscriptionTypes().subscribe({
      next: (data) => {
        this.subscriptions = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des abonnements';
        this.loading = false;
        console.error(err);
      }
    });
  }

  showCreateModal() {
    this.isEditMode = false;
    this.currentSubscription = {
      nom: '',
      description: '',
      cout: 0,
      duree: 30,
      nombreScrapingMax: 100,
      type: 'MENSUEL',
      estActif: true
    };
    this.showModal = true;
  }

  editSubscription(subscription: SubscriptionType) {
    this.isEditMode = true;
    this.currentSubscription = { ...subscription };
    this.showModal = true;
  }

  saveSubscription() {
    // Ajouter une valeur par défaut pour nombreProfilsMax si elle n'existe pas
    const subscriptionData = {
      ...this.currentSubscription,
      nombreProfilsMax: this.currentSubscription.nombreProfilsMax || 0
    };

    if (this.isEditMode && subscriptionData.id) {
      this.subscriptionService.updateSubscriptionType(subscriptionData.id, subscriptionData).subscribe({
        next: () => {
          this.loadSubscriptions();
          this.closeModal();
        },
        error: (err) => {
          alert('Erreur lors de la mise à jour');
          console.error(err);
        }
      });
    } else {
      this.subscriptionService.createSubscriptionType(subscriptionData).subscribe({
        next: () => {
          this.loadSubscriptions();
          this.closeModal();
        },
        error: (err) => {
          alert('Erreur lors de la création');
          console.error(err);
        }
      });
    }
  }

  toggleSubscription(subscription: SubscriptionType) {
    if (!subscription.id) return;
    this.subscriptionService.toggleSubscriptionTypeStatus(subscription.id).subscribe({
      next: () => {
        this.loadSubscriptions();
      },
      error: (err) => {
        alert('Erreur lors du changement de statut');
        console.error(err);
      }
    });
  }

  viewDetails(subscription: SubscriptionType) {
    this.selectedSubscription = subscription;
    this.showDetailsModal = true;
  }

  closeDetailsModal() {
    this.showDetailsModal = false;
    this.selectedSubscription = null;
  }

  deleteSubscription(subscription: SubscriptionType) {
    this.subscriptionToDelete = subscription;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.subscriptionToDelete && this.subscriptionToDelete.id) {
      this.subscriptionService.deleteSubscriptionType(this.subscriptionToDelete.id).subscribe({
        next: () => {
          this.loadSubscriptions();
          this.closeDeleteModal();
        },
        error: (err) => {
          alert('Erreur lors de la suppression');
          console.error(err);
          this.closeDeleteModal();
        }
      });
    }
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
    this.subscriptionToDelete = null;
  }

  onTypeChange(type: string) {
    switch(type) {
      case 'MENSUEL':
        this.currentSubscription.duree = 30;
        break;
      case 'TRIMESTRIEL':
        this.currentSubscription.duree = 90;
        break;
      case 'ANNUEL':
        this.currentSubscription.duree = 365;
        break;
      default:
        this.currentSubscription.duree = 30;
    }
  }

  closeModal() {
    this.showModal = false;
    this.isEditMode = false;
  }

  get subscriptionTypes() {
    return this.subscriptions;
  }
}
