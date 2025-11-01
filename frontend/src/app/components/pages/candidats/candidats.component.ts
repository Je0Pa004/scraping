import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CandidatsService } from '../../../core/services/candidats.service';
import { Candidat } from '../../../core/models/candidat.model';
import { NotificationService } from '../../../core/services/notification.service';
import { WsService } from '../../../core/services/ws.service';

@Component({
  selector: 'app-candidats',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './candidats.component.html',
  styleUrls: ['./candidats.component.css']
})
export class CandidatsComponent implements OnInit {
  loading = false;
  candidats: Candidat[] = [];

  // Invitation form
  email = '';
  nom = '';
  telephone = '';
  notes = '';
  profilPublicId = '';

  constructor(private srv: CandidatsService, private notify: NotificationService, private ws: WsService) {}

  ngOnInit(): void {
    this.refresh();
    // Écoute WebSocket événements backend (ex: { type: 'cv_recu', candidat: {...} })
    this.ws.connect('/ws').subscribe((evt: any) => {
      const type = (evt?.type || '').toString();
      if (type === 'cv_recu') {
        const c = evt?.candidat || {};
        const who = c?.email || c?.nom || 'candidat';
        this.notify.showSuccess(`CV reçu pour ${who}`);
        this.refresh();
      }
    });
  }

  refresh() {
    this.loading = true;
    this.srv.getAll().subscribe({
      next: (list) => { this.candidats = list || []; this.loading = false; },
      error: () => { this.loading = false; this.notify.showError("Erreur lors du chargement des candidats"); }
    });
  }

  inviter() {
    if (!this.email) { this.notify.showWarning('Email requis'); return; }
    const payload = { email: this.email, nom: this.nom || undefined, telephone: this.telephone || undefined, profilPublicId: this.profilPublicId || undefined, notes: this.notes || undefined };
    this.srv.inviter(payload).subscribe({
      next: () => {
        this.notify.showSuccess('Invitation envoyée');
        this.email = this.nom = this.telephone = this.notes = this.profilPublicId = '';
        this.refresh();
      },
      error: () => this.notify.showError("Échec de l'invitation")
    });
  }
}
