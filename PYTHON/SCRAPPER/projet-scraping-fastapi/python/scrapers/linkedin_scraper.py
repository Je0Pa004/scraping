"""
Scraper LinkedIn via API personnalisée uniquement
"""
import os
import re
import requests
import time
from typing import List, Dict, Optional
from utils.logger import setup_logger

logger = setup_logger()


class LinkedInScraper:
    """Scraper pour LinkedIn (API personnalisée uniquement)"""

    def __init__(self):
        # Configuration API obligatoire
        self.api_base_url = os.getenv('LINKEDIN_API_BASE_URL')
        self.api_token = os.getenv('LINKEDIN_API_TOKEN')

        if not self.api_base_url or not self.api_token:
            raise RuntimeError(
                "Configuration LinkedIn API incomplète. "
                "Veuillez définir LINKEDIN_API_BASE_URL et LINKEDIN_API_TOKEN dans le fichier .env"
            )

        self.session: Optional[requests.Session] = None
        self._init_api_session()
        logger.info("LinkedIn scraper initialisé avec l'API personnalisée")

    def _init_api_session(self):
        self.session = requests.Session()
        self.session.headers.update({
            'Authorization': f'Bearer {self.api_token}',
            'Accept': 'application/json'
        })

    def search_profiles(
        self,
        titre: Optional[str] = None,
        secteur: Optional[str] = None,
        localisation: Optional[str] = None,
        entreprise: Optional[str] = None,
        emploi: Optional[str] = None,
        taille_entreprise: Optional[str] = None,
        max_results: int = 10,
    ) -> List[Dict]:
        """Recherche de profils LinkedIn via l'API personnalisée uniquement."""
        if not self.session:
            raise RuntimeError("Session API non initialisée")

        logger.info(f"Recherche LinkedIn: titre={titre}, secteur={secteur}, localisation={localisation}")

        return self._api_search_profiles(
            titre=titre,
            secteur=secteur,
            localisation=localisation,
            entreprise=entreprise,
            emploi=emploi,
            taille_entreprise=taille_entreprise,
            max_results=max_results,
        )

    def scrape_profile_details(self, profile_url: str) -> Dict:
        """Détails d'un profil via l'API personnalisée uniquement."""
        if not self.session:
            raise RuntimeError("Session API non initialisée")

        return self._api_profile_details(profile_url)

    # ===== API perso =====
    def _api_search_profiles(
        self,
        titre: Optional[str],
        secteur: Optional[str],
        localisation: Optional[str],
        entreprise: Optional[str],
        emploi: Optional[str],
        taille_entreprise: Optional[str],
        max_results: int,
    ) -> List[Dict]:
        if not (self.session and self.api_base_url):
            raise RuntimeError("API LinkedIn non configurée")
        endpoint = f"{self.api_base_url.rstrip('/')}/profiles/search"
        params = {
            'title': titre,
            'industry': secteur,
            'location': localisation,
            'company': entreprise,
            'job': emploi,
            'company_size': taille_entreprise,
            'limit': max_results,
        }
        params = {k: v for k, v in params.items() if v is not None}
        r = self.session.get(endpoint, params=params, timeout=30)
        r.raise_for_status()
        data = r.json() or []
        out: List[Dict] = []
        for it in data:
            out.append({
                'nom': it.get('name') or it.get('full_name') or 'N/A',
                'description': it.get('headline') or it.get('title') or 'N/A',
                'telephone': it.get('phone') or 'N/A',
                'email': it.get('email') or 'N/A',
                'localisation': it.get('location') or 'N/A',
                'profile_url': it.get('profile_url') or it.get('url'),
            })
        return out

    def _api_profile_details(self, profile_url: str) -> Dict:
        if not (self.session and self.api_base_url):
            raise RuntimeError("API LinkedIn non configurée")
        endpoint = f"{self.api_base_url.rstrip('/')}/profiles/details"
        r = self.session.get(endpoint, params={'url': profile_url}, timeout=30)
        r.raise_for_status()
        it = r.json() or {}
        return {
            'url': profile_url,
            'nom': it.get('name') or 'N/A',
            'titre': it.get('headline') or it.get('title') or 'N/A',
            'description': it.get('summary') or 'N/A',
            'localisation': it.get('location') or 'N/A',
            'telephone': it.get('phone') or 'N/A',
            'email': it.get('email') or 'N/A',
            'experiences': it.get('experiences') or [],
            'formations': it.get('education') or [],
            'competences': it.get('skills') or [],
        }

    # Helpers Hunter supprimés: enrichissement réalisé via utils.contact_enricher
