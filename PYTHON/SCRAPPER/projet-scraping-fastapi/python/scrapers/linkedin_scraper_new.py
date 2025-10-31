"""
Scraper LinkedIn utilisant le token Bearer
"""
import os
import requests
from typing import List, Dict, Optional
from dotenv import load_dotenv
from utils.logger import setup_logger

logger = setup_logger()

# Charger automatiquement les variables depuis le fichier .env
load_dotenv()


class LinkedInScraper:
    """Scraper pour LinkedIn utilisant le token Bearer"""

    def __init__(self):
        # Récupération des variables d'environnement
        self.api_token = os.getenv('LINKEDIN_API_TOKEN')
        self.api_base_url = os.getenv('LINKEDIN_API_BASE_URL', 'https://api.linkedin.com/v2')

        if not self.api_token:
            raise RuntimeError(
                "Token LinkedIn requis. "
                "Définissez LINKEDIN_API_TOKEN dans le fichier .env"
            )

        self.session = requests.Session()
        self.session.headers.update({
            'Authorization': f'Bearer {self.api_token}',
            'Content-Type': 'application/json',
            'X-Restli-Protocol-Version': '2.0.0'
        })

        logger.info(f"LinkedIn scraper initialisé avec token Bearer et API {self.api_base_url}")

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
        """Recherche de profils LinkedIn avec l'API officielle"""
        logger.info(f"Recherche LinkedIn: titre={titre}, localisation={localisation}")

        try:
            # Construire les paramètres de recherche
            params = []

            if titre:
                params.append(f"title={titre}")
            if localisation:
                params.append(f"location={localisation}")
            if secteur:
                params.append(f"industry={secteur}")
            if entreprise:
                params.append(f"company={entreprise}")

            # Ajouter la limite
            params.append(f"limit={max_results}")

            # Construire l'URL de recherche
            query_string = "&".join(params)
            search_url = f"{self.api_base_url}/people/search?{query_string}"

            logger.info(f"URL de recherche: {search_url}")

            # Faire la requête
            response = self.session.get(search_url, timeout=30)
            response.raise_for_status()

            data = response.json()
            results = []

            for element in data.get('elements', []):
                try:
                    profile_id = element.get('id')
                    if profile_id:
                        profile_details = self._get_profile_details(profile_id)
                        result = {
                            'nom': f"{profile_details.get('firstName', {}).get('localized', {}).get('fr_FR', '')} {profile_details.get('lastName', {}).get('localized', {}).get('fr_FR', '')}",
                            'description': profile_details.get('headline', {}).get('localized', {}).get('fr_FR', ''),
                            'localisation': profile_details.get('location', {}).get('name', localisation or 'N/A'),
                            'profile_url': f"https://www.linkedin.com/in/{profile_id}",
                            'email': 'N/A',
                            'telephone': 'N/A',
                            'profile_id': profile_id,
                        }
                        results.append(result)
                except Exception as e:
                    logger.warning(f"Erreur lors de la récupération du profil {element.get('id')}: {e}")
                    continue

            logger.info(f"Recherche terminée - {len(results)} profils trouvés")
            return results

        except Exception as e:
            logger.error(f"Erreur lors de la recherche: {e}")
            raise RuntimeError(f"Erreur de recherche LinkedIn: {e}")

    def _get_profile_details(self, profile_id: str) -> Dict:
        """Récupérer les détails d'un profil spécifique"""
        profile_url = f"{self.api_base_url}/people/{profile_id}"
        response = self.session.get(profile_url, timeout=30)
        response.raise_for_status()
        return response.json()

    def scrape_profile_details(self, profile_url: str) -> Dict:
        """Récupérer les détails complets d'un profil"""
        try:
            profile_id = profile_url.split('/in/')[-1].split('/')[0]
            profile_details = self._get_profile_details(profile_id)
            return {
                'url': profile_url,
                'nom': f"{profile_details.get('firstName', {}).get('localized', {}).get('fr_FR', '')} {profile_details.get('lastName', {}).get('localized', {}).get('fr_FR', '')}",
                'titre': profile_details.get('headline', {}).get('localized', {}).get('fr_FR', ''),
                'description': profile_details.get('summary', ''),
                'localisation': profile_details.get('location', {}).get('name', ''),
                'telephone': 'N/A',
                'email': 'N/A',
                'experiences': profile_details.get('experience', []),
                'formations': profile_details.get('education', []),
                'competences': profile_details.get('skills', []),
            }

        except Exception as e:
            logger.error(f"Erreur lors de la récupération des détails du profil: {e}")
            return {
                'url': profile_url,
                'nom': 'N/A',
                'titre': 'N/A',
                'description': 'N/A',
                'localisation': 'N/A',
                'telephone': 'N/A',
                'email': 'N/A',
                'experiences': [],
                'formations': [],
                'competences': [],
            }
