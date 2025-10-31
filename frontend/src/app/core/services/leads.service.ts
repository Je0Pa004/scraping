import { Injectable } from '@angular/core';
import { Lead, LeadMessage, LeadProfileRef, LeadStatus } from '../models/lead.model';

const STORAGE_KEY = 'leads_storage_v1';

@Injectable({ providedIn: 'root' })
export class LeadsService {
  private read(): Lead[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as Lead[]) : [];
    } catch {
      return [];
    }
  }

  private write(items: Lead[]) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }

  getAll(): Lead[] {
    return this.read();
  }

  findById(id: string): Lead | undefined {
    return this.read().find(l => l.id === id);
  }

  private makeId(): string {
    return Math.random().toString(36).slice(2) + Date.now().toString(36);
  }

  private identity(p?: LeadProfileRef): string {
    const parts = [p?.publicId || '', p?.email || '', p?.urlSource || '', p?.nom || ''];
    return parts.join('|').toLowerCase();
  }

  upsertFromProfile(profile: LeadProfileRef): Lead {
    const all = this.read();
    const key = this.identity(profile);
    let lead = all.find(l => this.identity(l.profile) === key);
    const now = new Date().toISOString();
    if (!lead) {
      lead = {
        id: this.makeId(),
        profile: profile,
        status: 'A_CONTacter',
        createdAt: now,
        updatedAt: now,
        messages: []
      } as Lead;
      all.unshift(lead);
    } else {
      lead.profile = { ...lead.profile, ...profile };
      lead.updatedAt = now;
    }
    this.write(all);
    return lead;
  }

  updateStatus(id: string, status: LeadStatus) {
    const all = this.read();
    const idx = all.findIndex(l => l.id === id);
    if (idx >= 0) {
      all[idx].status = status;
      all[idx].updatedAt = new Date().toISOString();
      this.write(all);
    }
  }

  addMessage(leadId: string, m: Omit<LeadMessage, 'id'>): LeadMessage | null {
    const all = this.read();
    const idx = all.findIndex(l => l.id === leadId);
    if (idx < 0) return null;
    const msg: LeadMessage = { id: this.makeId(), ...m } as LeadMessage;
    all[idx].messages = all[idx].messages || [];
    all[idx].messages.push(msg);
    all[idx].lastAction = msg.sentAt || msg.scheduledAt || new Date().toISOString();
    all[idx].updatedAt = new Date().toISOString();
    this.write(all);
    return msg;
  }

  remove(id: string) {
    const all = this.read().filter(l => l.id !== id);
    this.write(all);
  }

  stats() {
    const all = this.read();
    const total = all.length;
    const messages = all.reduce((acc, l) => acc + (l.messages?.length || 0), 0);
    const scheduled = all.reduce((acc, l) => acc + (l.messages?.filter(m => !!m.scheduledAt && !m.sentAt).length || 0), 0);
    const byStatus: Record<string, number> = {};
    for (const l of all) {
      byStatus[l.status] = (byStatus[l.status] || 0) + 1;
    }
    return { total, messages, scheduled, byStatus };
  }
}
