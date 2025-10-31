export type SubscriptionTypeEnum = 'MENSUEL' | 'TRIMESTRIEL' | 'ANNUEL';

// Aligne avec AbonnementResponse (backend)
export interface Subscription {
  publicId: string;               // UUID
  dateDebut: string;              // ISO date
  dateFin: string;                // ISO date
  statut: boolean;                // actif ?
  nombreScraping: number;         // compteur utilisé
  prix: number;                   // BigDecimal côté back, number côté front
  utilisateurPublicId: string;    // UUID user
  typeAbonnement: SubscriptionType; // Type détaillé
}

// Aligne avec TypeAbonnementResponse (backend)
export interface SubscriptionType {
  id: number;                     // id interne
  publicId: string;               // UUID
  nom: string;
  description?: string;
  cout: number;
  duree: number;                  // jours
  nombreScrapingMax: number;
  nombreProfilsMax: number;
  type: SubscriptionTypeEnum;     // MENSUEL|TRIMESTRIEL|ANNUEL
  estActif: boolean;
}
