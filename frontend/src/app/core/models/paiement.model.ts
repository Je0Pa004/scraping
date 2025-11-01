export type StatutPaiement = 'SUCCESS' | 'PENDING' | 'FAILED' | 'REFUNDED';

export interface Paiement {
  publicId: string;
  utilisateurPublicId?: string;
  datePaiement: string; // ISO
  montant: number;
  methode: 'CARTE' | 'MOBILE_MONEY' | string;
  statut: StatutPaiement | string;
  typeAbonnement?: {
    id?: number;
    publicId?: string;
    nom?: string;
    description?: string;
  };
}
