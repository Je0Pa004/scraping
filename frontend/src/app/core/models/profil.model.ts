export interface Profil {
  id: number;
  utilisateurId: number;
  prenom: string;
  nom: string;
  telephone: string;
  adresse: string;
  ville: string;
  pays: string;
  urlLinkedin?: string;
  urlGithub?: string;
  urlPortfolio?: string;
  biographie: string;
  competences: string[];
  experiences: Experience[];
  formations: Formation[];
  dateCreation: Date;
  dateMiseAJour: Date;
  titre?: string;
  localisation?: string;
  entreprise?: string;
}

export interface Experience {
  id: number;
  entreprise: string;
  poste: string;
  dateDebut: Date;
  dateFin?: Date;
  description: string;
  estActuel: boolean;
}

export interface Formation {
  id: number;
  etablissement: string;
  diplome: string;
  domaine: string;
  dateDebut: Date;
  dateFin?: Date;
  mention?: string;
}
