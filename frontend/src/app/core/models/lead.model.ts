export type LeadStatus = 'A_CONTacter' | 'CONTACTE' | 'REPONDU' | 'ENTRETIEN' | 'REJETE';

export interface LeadMessage {
  id: string;
  subject: string;
  body: string;
  scheduledAt?: string;
  sentAt?: string;
}

export interface LeadProfileRef {
  publicId?: string;
  nom?: string;
  description?: string;
  email?: string;
  telephone?: string;
  urlSource?: string;
}

export interface Lead {
  id: string;
  profile: LeadProfileRef;
  status: LeadStatus;
  createdAt: string;
  updatedAt: string;
  lastAction?: string;
  messages: LeadMessage[];
}
