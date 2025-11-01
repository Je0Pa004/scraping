export type TypeAbonnementEnum = 'MENSUEL' | 'TRIMESTRIEL' | 'ANNUEL';

export interface TypeAbonnement {
  id: number;
  publicId: string;
  nom: string;
  description?: string;
  cout: number;
  duree: number; // jours
  nombreScrapingMax: number;
  nombreProfilsMax: number;
  type: TypeAbonnementEnum;
  estActif: boolean;
}

export interface Abonnement {
  publicId: string;
  dateDebut: string; // ISO date
  dateFin: string;   // ISO date
  statut: boolean;
  nombreScraping: number;
  prix: number;
  utilisateurPublicId: string;
  typeAbonnement: TypeAbonnement;
}
