"""
Service d'analyse de CV utilisant Hugging Face/spaCy
"""
from typing import Dict, List, Optional
import re
import spacy
from utils.logger import setup_logger

logger = setup_logger()


class CVAnalyzer:
    """Analyseur de CV basé sur l'IA"""
    
    def __init__(self):
        """Initialisation de l'analyseur de CV"""
        try:
            self.nlp = spacy.load("fr_core_news_md")
            logger.info("Modèle spaCy (fr_core_news_md) chargé pour l'analyse de CV")
        except OSError:
            try:
                self.nlp = spacy.load("fr_core_news_sm")
                logger.warning("Modèle md indisponible, fallback fr_core_news_sm")
            except Exception:
                self.nlp = None
                logger.error("Aucun modèle spaCy disponible. Fallback regex/keywords uniquement.")
        
        # Liste simple de compétences communes (exemple, à enrichir)
        self.default_skills = [
            'python','java','javascript','typescript','sql','nosql','postgresql','mysql','mongodb',
            'docker','kubernetes','aws','azure','gcp','linux','git','ci/cd','terraform',
            'react','vue','angular','node','django','flask','fastapi','spring','maven','gradle',
            'nlp','spacy','transformers','pytorch','tensor','machine learning','deep learning'
        ]
    
    def analyze_cv(self, cv_text: str) -> Dict:
        """
        Analyse un CV et extrait les informations clés
        
        Args:
            cv_text: Texte du CV
            
        Returns:
            Informations extraites (compétences, expérience, formation, etc.)
        """
        if not cv_text or not cv_text.strip():
            return {
                'skills': [],
                'experiences': [],
                'emails': [],
                'telephones': []
            }
        
        emails = self._extract_emails(cv_text)
        phones = self._extract_phones(cv_text)
        skills = self.extract_skills(cv_text)
        experiences = self.extract_experience(cv_text)
        
        return {
            'skills': skills,
            'experiences': experiences,
            'emails': emails,
            'telephones': phones
        }
    
    def extract_skills(self, cv_text: str) -> List[str]:
        """
        Extrait les compétences d'un CV
        
        Args:
            cv_text: Texte du CV
            
        Returns:
            Liste de compétences identifiées
        """
        text = cv_text.lower()
        found = set()
        
        # 1) Matching simple par mots-clés
        for skill in self.default_skills:
            if skill.lower() in text:
                found.add(skill)
        
        # 2) NLP: détecter les entités/tokens techniques si modèle dispo
        if self.nlp:
            doc = self.nlp(text)
            # Ajouter les tokens en majuscules fréquents (acronymes techniques)
            for token in doc:
                if len(token.text) >= 2 and token.text.isupper() and token.is_alpha:
                    found.add(token.text.lower())
        
        return sorted(found)
    
    def extract_experience(self, cv_text: str) -> List[Dict]:
        """
        Extrait les expériences professionnelles
        
        Args:
            cv_text: Texte du CV
            
        Returns:
            Liste d'expériences professionnelles
        """
        experiences: List[Dict] = []
        lines = [l.strip() for l in cv_text.splitlines() if l.strip()]
        
        # Regex simples pour périodes et postes
        date_pattern = re.compile(r'(\d{2}/\d{4}|\d{4})\s*(?:-|à|au|->|jusqu\'à)\s*(\d{2}/\d{4}|\d{4}|présent|aujourd\'hui)', re.IGNORECASE)
        role_sep = re.compile(r'\s*-\s*|\s*:\s*')
        
        for line in lines:
            m = date_pattern.search(line)
            if m:
                period = m.group(0)
                before = line[:m.start()].strip()
                after = line[m.end():].strip()
                
                role = before
                company = ''
                # Heuristique: rôle - entreprise
                parts = role_sep.split(before)
                if len(parts) >= 2:
                    role = parts[0].strip()
                    company = parts[1].strip()
                
                if not company and self.nlp:
                    # Attempt NER to find ORG
                    doc = self.nlp(line)
                    for ent in doc.ents:
                        if ent.label_ in ("ORG", "MISC"):
                            company = ent.text
                            break
                
                experiences.append({
                    'periode': period,
                    'poste': role or 'N/A',
                    'entreprise': company or 'N/A',
                    'description': after
                })
        
        return experiences

    def _extract_emails(self, text: str) -> List[str]:
        pattern = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b'
        return sorted(set(re.findall(pattern, text)))

    def _extract_phones(self, text: str) -> List[str]:
        pattern = r'(?:\+33|0)[1-9](?:[\s.-]?\d{2}){4}'
        return sorted(set(re.findall(pattern, text)))
