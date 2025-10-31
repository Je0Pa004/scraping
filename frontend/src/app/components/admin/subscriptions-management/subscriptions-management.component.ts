import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SubscriptionService } from '../../../core/services/subscription.service';

interface SubscriptionTypeVM {
  id?: number;
  nom: string;
  description?: string;
  cout: number;
  duree: number; // jours
  nombreScrapingMax: number;
  nombreProfilsMax: number;
  type: 'MENSUEL' | 'TRIMESTRIEL' | 'ANNUEL';
  estActif: boolean;
}

@Component({
  selector: 'app-subscriptions-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './subscriptions-management.component.html',
  styleUrl: './subscriptions-management.component.css'
})
export class SubscriptionsManagementComponent implements OnInit {
  subscriptions: SubscriptionTypeVM[] = [];
  loading = false;
  showModal = false;
  modalMode: 'create' | 'edit' = 'create';
  selectedSubscription: SubscriptionTypeVM = this.getEmptySubscription();

  constructor(private subscriptionService: SubscriptionService) {}

  ngOnInit() {
    this.loadSubscriptions();
  }

  loadSubscriptions() {
    this.loading = true;
    this.subscriptionService.getAllSubscriptionTypes().subscribe({
      next: (data) => {
        this.subscriptions = data as any;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des abonnements', err);
        this.loading = false;
      }
    });
  }

  openCreateModal() {
    this.modalMode = 'create';
    this.selectedSubscription = this.getEmptySubscription();
    this.showModal = true;
  }

  editSubscription(subscription: SubscriptionTypeVM) {
    this.modalMode = 'edit';
    this.selectedSubscription = { ...subscription };
    this.showModal = true;
  }

  saveSubscription() {
    if (this.modalMode === 'create') {
      this.subscriptionService.createSubscriptionType(this.selectedSubscription).subscribe({
        next: () => {
          this.loadSubscriptions();
          this.closeModal();
        },
        error: (err) => {
          alert('Erreur lors de la création de l\'abonnement');
          console.error(err);
        }
      });
    } else {
      this.subscriptionService.updateSubscriptionType(this.selectedSubscription.id!, this.selectedSubscription).subscribe({
        next: () => {
          this.loadSubscriptions();
          this.closeModal();
        },
        error: (err) => {
          alert('Erreur lors de la mise à jour de l\'abonnement');
          console.error(err);
        }
      });
    }
  }

  deleteSubscription(id: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet abonnement ?')) {
      this.subscriptionService.deleteSubscriptionType(id).subscribe({
        next: () => {
          this.loadSubscriptions();
        },
        error: (err) => {
          alert('Erreur lors de la suppression de l\'abonnement');
          console.error(err);
        }
      });
    }
  }

  toggleStatus(subscription: SubscriptionTypeVM) {
    subscription.estActif = !subscription.estActif;
    this.subscriptionService.updateSubscriptionType(subscription.id!, subscription).subscribe({
      next: () => {
        this.loadSubscriptions();
      },
      error: (err) => {
        alert('Erreur lors de la mise à jour du statut');
        console.error(err);
        subscription.estActif = !subscription.estActif; // Revert on error
      }
    });
  }

  closeModal() {
    this.showModal = false;
    this.selectedSubscription = this.getEmptySubscription();
  }

  private getEmptySubscription(): SubscriptionTypeVM {
    return {
      nom: '',
      description: '',
      cout: 0,
      duree: 30,
      nombreScrapingMax: 10,
      nombreProfilsMax: 50,
      type: 'MENSUEL',
      estActif: true
    };
  }
}
