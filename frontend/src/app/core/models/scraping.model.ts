import type { ProfilScraper } from './profil-scraper.model';

export type ApiSource = 'GOOGLE_MAP' | 'LINKEDIN' | string;
export type ScrapingStatus = 'EN_COURS' | 'TERMINE' | 'ECHEC' | string;

export interface Scraping {
  publicId: string;
  dateDemande: string; // ISO
  source: ApiSource;
  statut: ScrapingStatus;
  titre: string;
  secteur: string;
  localisation: string;
  entreprise: string;
  emploi: string;
  tailleEntreprise: string;
  nombreProfilScrape: number;
  utilisateurPublicId?: string; // pratique côté front
  profils?: ProfilScraper[];
}
