import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SubscriptionService } from '../../../core/services/subscription.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-my-payments',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-payments.component.html',
  styleUrl: './my-payments.component.css'
})
export class MyPaymentsComponent implements OnInit {
  isLoading = false;
  payments: any[] = [];

  constructor(private subs: SubscriptionService, private notify: NotificationService) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.isLoading = true;
    this.subs.getPayments().subscribe({
      next: (items) => {
        this.payments = items || [];
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.notify.showError('Erreur lors du chargement de vos paiements');
      }
    });
  }

  statusBadgeClass(s: string): string {
    switch ((s || '').toUpperCase()) {
      case 'SUCCESS':
      case 'SUCCEEDED':
        return 'bg-green-100 text-green-700';
      case 'PENDING':
        return 'bg-orange-100 text-orange-700';
      case 'FAILED':
        return 'bg-red-100 text-red-700';
      case 'REFUNDED':
        return 'bg-blue-100 text-blue-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  }
}
