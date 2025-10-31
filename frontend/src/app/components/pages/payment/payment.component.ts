import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SubscriptionType } from '../../../core/models/subscription.model';
import { SubscriptionService } from '../../../core/services/subscription.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.css'
})
export class PaymentComponent implements OnInit {
  selectedSubscription: SubscriptionType | null = null;
  paymentMethod: string = 'card';
  
  // Card payment
  cardNumber: string = '';
  cardExpiry: string = '';
  cardCvc: string = '';
  
  // Mobile money
  phoneCountry: string = '+228';
  phoneNumber: string = '';
  
  // Billing info
  companyName: string = '';
  billingEmail: string = '';
  address: string = '';
  city: string = '';
  postalCode: string = '';
  
  isProcessing: boolean = false;
  paymentHistory: any[] = [];

  constructor(
    private router: Router,
    private subscriptionService: SubscriptionService,
    private authService: AuthService,
    private notification: NotificationService
  ) {
    // Get subscription from navigation state
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras.state) {
      this.selectedSubscription = navigation.extras.state['subscription'];
    }
  }

  ngOnInit(): void {
    // If no subscription selected, redirect to subscriptions page
    if (!this.selectedSubscription) {
      const state = history.state;
      if (state && state.subscription) {
        this.selectedSubscription = state.subscription;
      } else {
        this.router.navigate(['/app/subscriptions']);
        return;
      }
    }
    
    // Pre-fill user info
    const user = this.authService.getCurrentUser();
    if (user) {
      this.billingEmail = user.email || '';
      this.companyName = (user as any).entreprise || (user as any).company || '';
    }
    
    // Load payment history
    this.loadPaymentHistory();
  }

  loadPaymentHistory(): void {
    this.subscriptionService.getPayments().subscribe({
      next: (payments) => {
        this.paymentHistory = payments;
      },
      error: (error) => {
        console.error('Error loading payment history:', error);
      }
    });
  }

  getTotalAmount(): number {
    return this.selectedSubscription?.cout || 0;
  }

  confirmPayment(): void {
    if (!this.selectedSubscription) {
      this.notification.showWarning('Aucun abonnement sélectionné');
      return;
    }

    console.log('Selected subscription:', this.selectedSubscription);

    // Check if publicId exists, otherwise use id
    const subscriptionId = this.selectedSubscription.publicId || this.selectedSubscription.id;
    
    if (!subscriptionId) {
      this.notification.showError('Erreur: Identifiant d\'abonnement manquant');
      console.error('Missing subscription ID:', this.selectedSubscription);
      return;
    }

    // Validate payment method
    if (this.paymentMethod === 'card') {
      if (!this.cardNumber || !this.cardExpiry || !this.cardCvc) {
        this.notification.showWarning('Veuillez remplir toutes les informations de carte');
        return;
      }
    } else if (this.paymentMethod === 'moov' || this.paymentMethod === 'flooz') {
      if (!this.phoneNumber) {
        this.notification.showWarning('Veuillez entrer votre numéro de téléphone');
        return;
      }
    }

    this.isProcessing = true;

    // Get current user
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.notification.showError('Erreur: Utilisateur non connecté');
      this.isProcessing = false;
      return;
    }

    console.log('Current user:', currentUser);

    // Create payment - get user ID
    const userId = (currentUser as any).id || (currentUser as any).publicId || (currentUser as any).public_id;
    
    if (!userId) {
      alert('Erreur: Identifiant utilisateur manquant');
      this.isProcessing = false;
      console.error('User object:', currentUser);
      return;
    }

    // Map payment method to backend enum values
    let methodeBackend = 'CARTE';
    if (this.paymentMethod === 'card') {
      methodeBackend = 'CARTE';
    } else if (this.paymentMethod === 'moov' || this.paymentMethod === 'flooz') {
      methodeBackend = 'MOBILE_MONEY';
    }

    const paymentData = {
      typeAbonnementId: subscriptionId,
      utilisateurPublicId: userId,
      montant: this.selectedSubscription.cout,
      methode: methodeBackend,
      statut: 'PENDING'
    };

    console.log('Payment data to send:', paymentData);

    this.subscriptionService.createPayment(paymentData).subscribe({
      next: (payment) => {
        console.log('Payment created:', payment);
        
        // Create subscription - add user ID
        const subscriptionData = {
          typeAbonnementPublicId: subscriptionId,
          utilisateurPublicId: userId,
          dateDebut: new Date().toISOString(),
          statut: true
        };

        console.log('Subscription data to send:', subscriptionData);

        this.subscriptionService.createSubscription(subscriptionData).subscribe({
          next: (subscription) => {
            console.log('Subscription created:', subscription);
            this.isProcessing = false;
            this.notification.showSuccess('Paiement effectué avec succès! Votre abonnement est maintenant actif.');
            // Reload payment history
            this.loadPaymentHistory();
            // Notify app to refresh subscription-based UI (un-grey buttons)
            this.subscriptionService.notifyChanged();
            this.router.navigate(['/app/dashboard']);
          },
          error: (error) => {
            console.error('Error creating subscription:', error);
            this.isProcessing = false;
            this.notification.showError('Erreur lors de la création de l\'abonnement');
          }
        });
      },
      error: (error) => {
        console.error('Error creating payment:', error);
        console.error('Error details:', error.error);
        this.isProcessing = false;
        const errorMsg = error.error?.message || error.message || 'Erreur lors du traitement du paiement';
        this.notification.showError('Erreur: ' + errorMsg);
      }
    });
  }

  downloadInvoice(payment: any): void {
    // Get current user for invoice details
    const currentUser = this.authService.getCurrentUser();
    
    // Create invoice HTML
    const invoiceHtml = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <title>Reçu de Paiement #${payment.publicId}</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 40px auto;
            padding: 20px;
            color: #333;
          }
          .header {
            text-align: center;
            border-bottom: 3px solid #4F46E5;
            padding-bottom: 20px;
            margin-bottom: 30px;
          }
          .header h1 {
            color: #4F46E5;
            margin: 0;
            font-size: 32px;
          }
          .header p {
            color: #666;
            margin: 5px 0;
          }
          .invoice-info {
            display: flex;
            justify-content: space-between;
            margin-bottom: 30px;
          }
          .invoice-info div {
            flex: 1;
          }
          .section-title {
            font-size: 14px;
            font-weight: bold;
            color: #666;
            text-transform: uppercase;
            margin-bottom: 10px;
          }
          .info-line {
            margin: 8px 0;
            line-height: 1.6;
          }
          .info-label {
            font-weight: 600;
            color: #333;
          }
          table {
            width: 100%;
            border-collapse: collapse;
            margin: 30px 0;
          }
          th {
            background: #4F46E5;
            color: white;
            padding: 15px;
            text-align: left;
            font-weight: 600;
          }
          td {
            padding: 15px;
            border-bottom: 1px solid #E5E7EB;
          }
          .total-row {
            background: #F3F4F6;
            font-weight: bold;
            font-size: 18px;
          }
          .status {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
          }
          .status-success {
            background: #D1FAE5;
            color: #065F46;
          }
          .status-pending {
            background: #FEF3C7;
            color: #92400E;
          }
          .footer {
            text-align: center;
            margin-top: 50px;
            padding-top: 20px;
            border-top: 2px solid #E5E7EB;
            color: #666;
            font-size: 14px;
          }
          @media print {
            body {
              margin: 0;
              padding: 20px;
            }
          }
        </style>
      </head>
      <body>
        <div class="header">
          <h1>REÇU DE PAIEMENT</h1>
          <p>Merci pour votre confiance</p>
        </div>

        <div class="invoice-info">
          <div>
            <div class="section-title">Informations Client</div>
            <div class="info-line"><span class="info-label">Nom :</span> ${currentUser ? ((currentUser as any).fullName || (currentUser as any).username) : 'N/A'}</div>
            <div class="info-line"><span class="info-label">Email :</span> ${currentUser ? (currentUser as any).username : 'N/A'}</div>
            <div class="info-line"><span class="info-label">ID Client :</span> ${payment.utilisateurPublicId}</div>
          </div>
          <div style="text-align: right;">
            <div class="section-title">Détails de la Facture</div>
            <div class="info-line"><span class="info-label">N° Facture :</span> #${payment.publicId.substring(0, 8).toUpperCase()}</div>
            <div class="info-line"><span class="info-label">Date :</span> ${new Date(payment.datePaiement).toLocaleDateString('fr-FR')}</div>
            <div class="info-line">
              <span class="info-label">Statut :</span> 
              <span class="status ${payment.statut === 'SUCCESS' ? 'status-success' : 'status-pending'}">
                ${payment.statut === 'SUCCESS' ? 'Payé' : payment.statut === 'PENDING' ? 'En attente' : 'Échoué'}
              </span>
            </div>
          </div>
        </div>

        <table>
          <thead>
            <tr>
              <th>Description</th>
              <th>Méthode</th>
              <th style="text-align: right;">Montant</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>${payment.typeAbonnement?.nom || 'Abonnement'}<br>
                  <small style="color: #666;">${payment.typeAbonnement?.description || ''}</small>
              </td>
              <td>${payment.methode === 'MOBILE_MONEY' ? 'Mobile Money' : payment.methode === 'CARTE' ? 'Carte Bancaire' : payment.methode}</td>
              <td style="text-align: right;">${payment.montant} FCFA</td>
            </tr>
            <tr class="total-row">
              <td colspan="2">TOTAL</td>
              <td style="text-align: right;">${payment.montant} FCFA</td>
            </tr>
          </tbody>
        </table>

        <div class="footer">
          <p><strong>Merci pour votre paiement !</strong></p>
          <p>Ce reçu confirme votre transaction. Conservez-le pour vos dossiers.</p>
          <p style="margin-top: 20px; font-size: 12px;">
            Document généré automatiquement le ${new Date().toLocaleDateString('fr-FR')} à ${new Date().toLocaleTimeString('fr-FR')}
          </p>
        </div>
      </body>
      </html>
    `;

    // Create a new window with the invoice
    const printWindow = window.open('', '_blank');
    if (printWindow) {
      printWindow.document.write(invoiceHtml);
      printWindow.document.close();
      
      // Wait for content to load then print
      printWindow.onload = () => {
        printWindow.print();
      };
    } else {
      this.notification.showWarning('Veuillez autoriser les pop-ups pour télécharger le reçu');
    }
  }
}
