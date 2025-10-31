import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LeadsService } from '../../../core/services/leads.service';
import { Lead, LeadStatus } from '../../../core/models/lead.model';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-my-candidates',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="p-6 max-w-7xl mx-auto">
    <div class="mb-6">
      <h1 class="text-3xl font-black text-gray-900 mb-1">👤 Mes candidats</h1>
      <p class="text-gray-600">Gérez votre pipeline de candidats</p>
    </div>

    <div class="bg-white rounded-2xl shadow-soft p-6">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="border-b border-gray-200">
              <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">Nom</th>
              <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">Poste</th>
              <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">Email</th>
              <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">Statut</th>
              <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">Dernière action</th>
              <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let l of leads" class="border-b border-gray-100">
              <td class="py-3 px-4 text-sm font-semibold text-gray-900">{{ l.profile.nom || '—' }}</td>
              <td class="py-3 px-4 text-sm text-gray-700">{{ l.profile.description || '—' }}</td>
              <td class="py-3 px-4 text-sm">
                <a *ngIf="l.profile.email" [href]="'mailto:' + l.profile.email" class="text-primary-600">{{ l.profile.email }}</a>
                <span *ngIf="!l.profile.email">—</span>
              </td>
              <td class="py-3 px-4 text-sm">
                <select class="px-2 py-1 border rounded-lg" [(ngModel)]="l.status" (ngModelChange)="onStatusChange(l, $event)">
                  <option *ngFor="let s of statuses" [value]="s">{{ labelStatus(s) }}</option>
                </select>
              </td>
              <td class="py-3 px-4 text-sm text-gray-600">{{ l.lastAction ? (l.lastAction | date:'short') : '—' }}</td>
              <td class="py-3 px-4 text-sm">
                <div class="flex gap-2">
                  <a *ngIf="l.profile.urlSource" [href]="l.profile.urlSource" target="_blank" class="btn-icon btn-icon--view" title="Voir source">
                    <i class="fa-solid fa-eye"></i>
                  </a>
                  <button class="btn-icon btn-icon--delete" (click)="remove(l.id)" title="Supprimer">
                    <i class="fa-solid fa-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  `,
  styles: [`
  `]
})
export class MyCandidatesComponent implements OnInit {
  leads: Lead[] = [];
  statuses: LeadStatus[] = ['A_CONTacter','CONTACTE','REPONDU','ENTRETIEN','REJETE'];

  constructor(private leadsService: LeadsService, private notification: NotificationService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh() {
    this.leads = this.leadsService.getAll();
  }

  onStatusChange(l: Lead, status: LeadStatus) {
    this.leadsService.updateStatus(l.id, status);
    this.refresh();
    const label = this.labelStatus(status);
    this.notification.showSuccess(`Statut mis à jour: ${label}`);
  }

  remove(id: string) {
    this.leadsService.remove(id);
    this.refresh();
    this.notification.showSuccess('Candidat supprimé');
  }

  labelStatus(s: LeadStatus | string): string {
    switch (s) {
      case 'A_CONTacter': return 'À contacter';
      case 'CONTACTE': return 'Contacté';
      case 'REPONDU': return 'Répondu';
      case 'ENTRETIEN': return 'Entretien';
      case 'REJETE': return 'Rejeté';
      default: return String(s);
    }
  }
}
