export type StatutCandidat = 'INVITE' | 'CV_RECU' | 'REJET' | 'RETENU' | 'EN_ATTENTE';

export interface Candidat {
  publicId: string;
  nom?: string;
  email: string;
  telephone?: string;
  statut: StatutCandidat;
  cvUrl?: string;
  dateInvitation?: string; // ISO
  dateReception?: string;  // ISO
  notes?: string;
  // Référence optionnelle vers un profil scrappé
  profilPublicId?: string;
}
