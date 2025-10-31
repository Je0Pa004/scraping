"""
Service de sélection des profils pertinents utilisant spaCy
"""
import spacy
from typing import List, Dict, Optional
from utils.logger import setup_logger

logger = setup_logger()


class ProfileSelector:
    """Sélectionneur de profils basé sur l'IA"""
    
    def __init__(self):
        """Initialisation du sélectionneur de profils"""
        try:
            # Charger le modèle spaCy français
            self.nlp = spacy.load("fr_core_news_md")
            logger.info("Modèle spaCy chargé avec succès")
        except OSError:
            logger.warning("Modèle spaCy non trouvé. Utilisation du modèle de base.")
            # Fallback vers un modèle plus simple
            try:
                self.nlp = spacy.load("fr_core_news_sm")
            except:
                logger.error("Aucun modèle spaCy disponible")
                self.nlp = None
    
    def select_relevant_profiles(self, profiles: List[Dict], criteria: Dict) -> List[Dict]:
        """
        Sélectionne les profils pertinents selon des critères
        
        Args:
            profiles: Liste de profils à analyser
            criteria: Critères de sélection (compétences, expérience, localisation, etc.)
            
        Returns:
            Liste de profils pertinents avec scores (triés par pertinence)
        """
        if not profiles:
            logger.warning("Aucun profil à analyser")
            return []
        
        logger.info(f"Analyse de {len(profiles)} profils avec critères: {criteria}")
        
        scored_profiles = []
        
        for profile in profiles:
            try:
                score = self.score_profile(profile, criteria)
                profile_with_score = profile.copy()
                profile_with_score['score_pertinence'] = score
                scored_profiles.append(profile_with_score)
            except Exception as e:
                logger.warning(f"Erreur lors du scoring du profil {profile.get('nom')}: {e}")
                continue
        
        # Trier par score décroissant
        scored_profiles.sort(key=lambda x: x.get('score_pertinence', 0), reverse=True)
        
        # Filtrer les profils avec un score minimum
        min_score = criteria.get('score_minimum', 0.3)
        relevant_profiles = [p for p in scored_profiles if p.get('score_pertinence', 0) >= min_score]
        
        logger.info(f"{len(relevant_profiles)} profils pertinents identifiés (score >= {min_score})")
        
        return relevant_profiles
    
    def score_profile(self, profile: Dict, criteria: Dict) -> float:
        """
        Calcule un score de pertinence pour un profil
        
        Args:
            profile: Profil à scorer
            criteria: Critères de scoring
            
        Returns:
            Score de pertinence (0-1)
        """
        if not self.nlp:
            # Fallback: scoring simple basé sur des mots-clés
            return self._simple_keyword_score(profile, criteria)
        
        total_score = 0.0
        weight_sum = 0.0
        
        # 1. Score de compétences (poids: 0.4)
        if 'competences_requises' in criteria:
            competences_score = self._score_competences(profile, criteria['competences_requises'])
            total_score += competences_score * 0.4
            weight_sum += 0.4
        
        # 2. Score d'expérience (poids: 0.3)
        if 'experience_requise' in criteria:
            experience_score = self._score_experience(profile, criteria['experience_requise'])
            total_score += experience_score * 0.3
            weight_sum += 0.3
        
        # 3. Score de localisation (poids: 0.2)
        if 'localisation' in criteria:
            location_score = self._score_localisation(profile, criteria['localisation'])
            total_score += location_score * 0.2
            weight_sum += 0.2
        
        # 4. Score de titre/poste (poids: 0.1)
        if 'titre_poste' in criteria:
            titre_score = self._score_titre(profile, criteria['titre_poste'])
            total_score += titre_score * 0.1
            weight_sum += 0.1
        
        # Normaliser le score
        final_score = total_score / weight_sum if weight_sum > 0 else 0.0
        
        return round(final_score, 3)
    
    def _score_competences(self, profile: Dict, competences_requises: List[str]) -> float:
        """Score basé sur les compétences"""
        if not competences_requises:
            return 0.5
        
        profile_text = f"{profile.get('description', '')} {profile.get('titre', '')}"
        
        if not profile_text.strip():
            return 0.0
        
        # Analyse NLP
        doc_profile = self.nlp(profile_text.lower())
        
        matches = 0
        for competence in competences_requises:
            doc_comp = self.nlp(competence.lower())
            # Similarité sémantique
            similarity = doc_profile.similarity(doc_comp)
            if similarity > 0.6:
                matches += 1
        
        return matches / len(competences_requises) if competences_requises else 0.0
    
    def _score_experience(self, profile: Dict, experience_requise: str) -> float:
        """Score basé sur l'expérience"""
        profile_text = profile.get('description', '')
        
        if not profile_text:
            return 0.0
        
        doc_profile = self.nlp(profile_text.lower())
        doc_exp = self.nlp(experience_requise.lower())
        
        return doc_profile.similarity(doc_exp)
    
    def _score_localisation(self, profile: Dict, localisation_requise: str) -> float:
        """Score basé sur la localisation"""
        profile_location = profile.get('localisation', '').lower()
        
        if not profile_location or profile_location == 'n/a':
            return 0.0
        
        # Correspondance exacte
        if localisation_requise.lower() in profile_location:
            return 1.0
        
        # Similarité sémantique
        doc_profile = self.nlp(profile_location)
        doc_loc = self.nlp(localisation_requise.lower())
        
        return doc_profile.similarity(doc_loc)
    
    def _score_titre(self, profile: Dict, titre_requis: str) -> float:
        """Score basé sur le titre du poste"""
        profile_title = profile.get('description', '') or profile.get('titre', '')
        
        if not profile_title:
            return 0.0
        
        doc_profile = self.nlp(profile_title.lower())
        doc_titre = self.nlp(titre_requis.lower())
        
        return doc_profile.similarity(doc_titre)
    
    def _simple_keyword_score(self, profile: Dict, criteria: Dict) -> float:
        """Scoring simple basé sur des mots-clés (fallback)"""
        profile_text = f"{profile.get('nom', '')} {profile.get('description', '')} {profile.get('titre', '')}".lower()
        
        if not profile_text.strip():
            return 0.0
        
        matches = 0
        total_criteria = 0
        
        # Vérifier les compétences
        if 'competences_requises' in criteria:
            for comp in criteria['competences_requises']:
                total_criteria += 1
                if comp.lower() in profile_text:
                    matches += 1
        
        # Vérifier la localisation
        if 'localisation' in criteria:
            total_criteria += 1
            if criteria['localisation'].lower() in profile_text:
                matches += 1
        
        # Vérifier le titre
        if 'titre_poste' in criteria:
            total_criteria += 1
            if criteria['titre_poste'].lower() in profile_text:
                matches += 1
        
        return matches / total_criteria if total_criteria > 0 else 0.0
