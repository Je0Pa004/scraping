import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeadsService } from '../../../core/services/leads.service';

@Component({
  selector: 'app-stats',
  standalone: true,
  imports: [CommonModule],
  template: `
  <div class="p-6 max-w-7xl mx-auto">
    <div class="mb-6">
      <h1 class="text-3xl font-black text-gray-900 mb-1">📈 Statistiques</h1>
      <p class="text-gray-600">Suivi de vos performances</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-xl shadow-soft p-5">
        <div class="text-sm text-gray-600">Candidats enregistrés</div>
        <div class="text-3xl font-black text-gray-900">{{ stats.total }}</div>
      </div>
      <div class="bg-white rounded-xl shadow-soft p-5">
        <div class="text-sm text-gray-600">Messages</div>
        <div class="text-3xl font-black text-primary-600">{{ stats.messages }}</div>
      </div>
      <div class="bg-white rounded-xl shadow-soft p-5">
        <div class="text-sm text-gray-600">Programmé</div>
        <div class="text-3xl font-black text-amber-600">{{ stats.scheduled }}</div>
      </div>
      <div class="bg-white rounded-xl shadow-soft p-5">
        <div class="text-sm text-gray-600">Dernière mise à jour</div>
        <div class="text-3xl font-black text-gray-900">{{ now | date:'shortTime' }}</div>
      </div>
    </div>

    <div class="bg-white rounded-2xl shadow-soft p-6">
      <h2 class="text-xl font-bold text-gray-900 mb-4">Pipeline</h2>
      <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
        <div class="p-4 rounded-xl border" *ngFor="let k of statusKeys">
          <div class="text-sm text-gray-600">{{ label(k) }}</div>
          <div class="text-2xl font-black text-gray-900">{{ stats.byStatus[k] || 0 }}</div>
        </div>
      </div>
    </div>
  </div>
  `
})
export class StatsComponent implements OnInit {
  stats: any = { total: 0, messages: 0, scheduled: 0, byStatus: {} };
  now = new Date();
  statusKeys: string[] = [];

  constructor(private leads: LeadsService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh() {
    this.stats = this.leads.stats();
    this.statusKeys = Object.keys(this.stats.byStatus || {'A_CONTacter':0,'CONTACTE':0,'REPONDU':0,'ENTRETIEN':0,'REJETE':0});
  }

  label(k: string) {
    switch (k) {
      case 'A_CONTacter': return 'À contacter';
      case 'CONTACTE': return 'Contacté';
      case 'REPONDU': return 'Répondu';
      case 'ENTRETIEN': return 'Entretien';
      case 'REJETE': return 'Rejeté';
      default: return k;
    }
  }
}
