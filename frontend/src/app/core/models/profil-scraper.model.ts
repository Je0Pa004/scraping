export interface ProfilScraper {
  publicId: string;
  nom: string;
  description: string;
  email: string;
  telephone: string;
  urlSource: string;
  statut: string; // ex: VALIDE, INVALIDE
  // Relation minimaliste pour lier au scraping
  scrapingPublicId?: string;
}
