import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ScrapingService } from '../../../core/services/scraping.service';
import { Profile } from '../../../core/models/profile.model';
import { LeadsService } from '../../../core/services/leads.service';
import { LeadProfileRef } from '../../../core/models/lead.model';
import { MessagesService } from '../../../core/services/messages.service';

@Component({
  selector: 'app-results',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.css']
})
export class ResultsComponent implements OnInit {
  profiles: Profile[] = [];
  loading = true;
  searchId: string | null = null;
  rawProfiles: any[] = [];
  selectedProfile: any = null;
  currentPage = 0;
  pageSize = 10;
  viewMode: 'grid' | 'table' = 'grid';
  isMessageOpen = false;
  messageSubject = '';
  messageBody = '';
  messageSchedule = '';
  messageLeadId: string | null = null;
  messageProfileRef: LeadProfileRef | null = null;
  showFilters = false;
  filterQuery = '';
  filterHasEmail = false;
  filterHasPhone = false;
  filterStatus = '';
  filterSource = '';

  constructor(private route: ActivatedRoute, private scrapingService: ScrapingService, private leads: LeadsService, private messages: MessagesService) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      this.searchId = params.get('searchId');
      if (this.searchId) {
        this.loadProfiles(this.searchId);
      } else {
        this.loadAllRawProfiles();
      }
    });
  }

  private loadProfiles(id: string) {
    this.loading = true;
    this.scrapingService.getProfilesByScraping(id).subscribe({
      next: (items: Profile[]) => {
        this.profiles = items || [];
        this.loading = false;
      },
      error: () => {
        this.profiles = [];
        this.loading = false;
      }
    });

    this.scrapingService.getRawProfilesByScraping(id).subscribe({
      next: (items: any[]) => {
        this.rawProfiles = items || [];
        this.currentPage = 0;
      },
      error: () => {
        this.rawProfiles = [];
      }
    });
  }

  private loadAllRawProfiles() {
    this.loading = true;
    this.scrapingService.getAllRawProfiles().subscribe({
      next: (items: any[]) => {
        this.rawProfiles = items || [];
        this.currentPage = 0;
        this.loading = false;
      },
      error: () => {
        this.rawProfiles = [];
        this.loading = false;
      }
    });
  }

  get totalProfilesCount(): number { return this.filteredRawProfiles.length; }

  get totalPages(): number {
    const total = this.totalProfilesCount;
    return Math.max(1, Math.ceil(total / this.pageSize));
  }

  get pagedRawProfiles(): any[] {
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    return this.filteredRawProfiles.slice(start, end);
  }

  get pageStartIndex(): number {
    if (this.totalProfilesCount === 0) return 0;
    return this.currentPage * this.pageSize + 1;
  }

  get pageEndIndex(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalProfilesCount);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  firstPage(): void { if (this.currentPage !== 0) this.currentPage = 0; }
  prevPage(): void { if (this.currentPage > 0) this.currentPage--; }
  nextPage(): void { if (this.currentPage < this.totalPages - 1) this.currentPage++; }
  lastPage(): void { const last = this.totalPages - 1; if (this.currentPage !== last) this.currentPage = last; }
  goToPage(i: number): void { if (i >= 0 && i < this.totalPages) this.currentPage = i; }

  onDeleteProfile(p: any): void {
    if (!p?.publicId) return;
    this.scrapingService.deleteProfileByPublicId(p.publicId).subscribe({
      next: () => {
        this.rawProfiles = this.rawProfiles.filter(x => `${x.publicId}` !== `${p.publicId}`);
      },
      error: () => {}
    });
  }

  onDownloadProfile(p: any): void {
    const data = {
      nom: p?.nom || '',
      description: p?.description || '',
      email: p?.email || '',
      telephone: p?.telephone || '',
      urlSource: p?.urlSource || '',
      statut: p?.statut || ''
    } as any;
    const header = Object.keys(data);
    const csv = [header.join(','), header.map(h => `"${(data as any)[h].toString().replace(/"/g,'""')}"`).join(',')].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.href = url;
    link.download = `${(p?.nom || 'profil').toString().replace(/\s+/g,'_')}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  onDetailProfile(p: any): void {
    this.selectedProfile = p;
  }

  onCloseDetail(): void {
    this.selectedProfile = null;
  }

  get filteredRawProfiles(): any[] {
    let list = this.rawProfiles || [];
    const q = this.filterQuery.trim().toLowerCase();
    if (q) {
      list = list.filter(p => {
        const fields = [p?.nom, p?.description, p?.email, p?.telephone, p?.urlSource].map((x: any) => (x || '').toString().toLowerCase());
        return fields.some((f: string) => f.includes(q));
      });
    }
    if (this.filterHasEmail) list = list.filter(p => !!p?.email);
    if (this.filterHasPhone) list = list.filter(p => !!p?.telephone);
    if (this.filterStatus) list = list.filter(p => (p?.statut || '').toString().toLowerCase() === this.filterStatus.toLowerCase());
    if (this.filterSource.trim()) list = list.filter(p => (p?.urlSource || '').toString().toLowerCase().includes(this.filterSource.trim().toLowerCase()));
    return list;
  }

  get availableStatuses(): string[] {
    const set = new Set<string>();
    for (const p of this.rawProfiles || []) {
      const s = (p?.statut || '').toString().trim();
      if (s) set.add(s);
    }
    return Array.from(set);
  }

  clearFilters(): void {
    this.filterQuery = '';
    this.filterHasEmail = false;
    this.filterHasPhone = false;
    this.filterStatus = '';
    this.filterSource = '';
    this.currentPage = 0;
  }

  private toLeadRef(p: any): LeadProfileRef {
    return {
      publicId: p?.publicId,
      nom: p?.nom,
      description: p?.description,
      email: p?.email,
      telephone: p?.telephone,
      urlSource: p?.urlSource
    } as LeadProfileRef;
  }

  onSaveProfile(p: any): void {
    const ref = this.toLeadRef(p);
    this.leads.upsertFromProfile(ref);
  }

  openMessage(p: any): void {
    const lead = this.leads.upsertFromProfile(this.toLeadRef(p));
    this.messageLeadId = lead.id;
    const name = p?.nom || '';
    const skill = (p?.description || '').split(',')[0] || '';
    this.messageSubject = `Proposition d'opportunité`;
    this.messageBody = `Bonjour ${name.split(' ')[0] || ''},\n\nJ'ai découvert votre profil et vos compétences en ${skill}. Nous serions ravis d'échanger sur une opportunité.\n\nBien cordialement,`;
    this.messageSchedule = '';
    this.isMessageOpen = true;
    this.messageProfileRef = this.toLeadRef(p);
  }

  closeMessage(): void {
    this.isMessageOpen = false;
    this.messageLeadId = null;
    this.messageSubject = '';
    this.messageBody = '';
    this.messageSchedule = '';
  }

  sendNow(): void {
    if (!this.messageLeadId) return;
    const toEmail = (this.messageProfileRef as any)?.email || '';
    const toName = (this.messageProfileRef as any)?.nom || '';
    this.messages.send({ toEmail, toName, subject: this.messageSubject, body: this.messageBody, profile: this.messageProfileRef || undefined }).subscribe({
      next: () => {
        this.leads.addMessage(this.messageLeadId!, { subject: this.messageSubject, body: this.messageBody, sentAt: new Date().toISOString() });
        this.closeMessage();
      },
      error: () => {
        alert('Erreur lors de l\'envoi du message');
      }
    });
  }

  scheduleSend(): void {
    if (!this.messageLeadId) return;
    const when = this.messageSchedule ? new Date(this.messageSchedule).toISOString() : new Date().toISOString();
    const toEmail = (this.messageProfileRef as any)?.email || '';
    const toName = (this.messageProfileRef as any)?.nom || '';
    this.messages.send({ toEmail, toName, subject: this.messageSubject, body: this.messageBody, scheduledAt: when, profile: this.messageProfileRef || undefined }).subscribe({
      next: () => {
        this.leads.addMessage(this.messageLeadId!, { subject: this.messageSubject, body: this.messageBody, scheduledAt: when });
        this.closeMessage();
      },
      error: () => {
        alert('Erreur lors de la programmation du message');
      }
    });
  }

  generateAI(): void {
    const name = (this.messageProfileRef as any)?.nom || '';
    const skills = (this.messageProfileRef as any)?.description || '';
    this.messages.generateText({ name, skills }).subscribe({
      next: (res) => {
        const txt = res?.text || '';
        if (txt) this.messageBody = txt;
      },
      error: () => {
        const intro = `Je souhaite vous présenter une opportunité correspondant à votre parcours.`;
        this.messageBody = `${this.messageBody}\n\n${intro}`.trim();
      }
    });
  }

  onExportResults(): void {
    const items = this.rawProfiles || [];
    const header = ['nom','description','email','telephone','urlSource','statut'];
    const rows = items.map(p => ({
      nom: p?.nom || '',
      description: p?.description || '',
      email: p?.email || '',
      telephone: p?.telephone || '',
      urlSource: p?.urlSource || '',
      statut: p?.statut || ''
    }));
    const csv = [header.join(','), ...rows.map(r => header.map(h => `"${(r as any)[h].toString().replace(/"/g,'""')}"`).join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.href = url;
    link.download = 'profiles.csv';
    link.click();
    URL.revokeObjectURL(url);
  }
}
