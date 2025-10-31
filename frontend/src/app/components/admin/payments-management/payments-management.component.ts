import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, Payment } from '../../../core/services/admin.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-payments-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payments-management.component.html',
  styleUrl: './payments-management.component.css'
})
export class PaymentsManagementComponent implements OnInit {
  payments: Payment[] = [];
  filteredPayments: Payment[] = [];
  loading = false;
  error: string | null = null;
  
  // Filters
  searchTerm = '';
  selectedStatus = 'all';
  selectedPlan = 'all';
  
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;
  
  // Stats
  get totalRevenue() {
    return this.payments
      .filter(p => p.status === 'SUCCEEDED')
      .reduce((sum, p) => sum + p.amount, 0);
  }
  
  get pendingPayments() {
    return this.payments.filter(p => p.status === 'PENDING').length;
  }
  
  get failedPayments() {
    return this.payments.filter(p => p.status === 'FAILED').length;
  }

  get displayedFrom() {
    return this.filteredPayments.length ? (this.currentPage - 1) * this.itemsPerPage + 1 : 0;
  }

  get displayedTo() {
    const to = this.currentPage * this.itemsPerPage;
    return Math.min(to, this.filteredPayments.length);
  }

  constructor(private adminService: AdminService, private notification: NotificationService) {}

  ngOnInit() {
    this.loadPayments();
  }

  loadPayments() {
    this.loading = true;
    this.adminService.getAllPayments().subscribe({
      next: (data) => {
        this.payments = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des paiements';
        this.loading = false;
        console.error(err);
        this.notification.showError('Erreur lors du chargement des paiements');
      }
    });
  }

  applyFilters() {
    this.filteredPayments = this.payments.filter(payment => {
      const matchesSearch = payment.user.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           payment.email.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           payment.transactionId.toLowerCase().includes(this.searchTerm.toLowerCase());
      const statusFilter = this.selectedStatus?.toUpperCase();
      const matchesStatus = this.selectedStatus === 'all' || payment.status === statusFilter;
      const matchesPlan = this.selectedPlan === 'all' || payment.plan === this.selectedPlan;
      
      return matchesSearch && matchesStatus && matchesPlan;
    });
    
    this.totalPages = Math.ceil(this.filteredPayments.length / this.itemsPerPage);
    this.currentPage = 1;
  }

  get paginatedPayments() {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.filteredPayments.slice(start, end);
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  markAsPaid(paymentId: string) {
    if (!paymentId) return;
    if (confirm('Confirmer le passage du paiement en "Payé" ?')) {
      this.adminService.updatePaymentStatus(paymentId, { status: 'SUCCESS' }).subscribe({
        next: () => {
          this.notification.showSuccess('Paiement validé');
          this.loadPayments();
        },
        error: (err) => {
          this.notification.showError('Erreur lors de la mise à jour du statut');
          console.error(err);
        }
      });
    }
  }

  refundPayment(paymentId: string) {
    if (!paymentId) return;
    if (confirm('Confirmer le remboursement du paiement ?')) {
      this.adminService.refundPayment(paymentId).subscribe({
        next: () => {
          this.notification.showSuccess('Remboursement effectué');
          this.loadPayments();
        },
        error: (err) => {
          this.notification.showError('Erreur lors du remboursement');
          console.error(err);
        }
      });
    }
  }
}
