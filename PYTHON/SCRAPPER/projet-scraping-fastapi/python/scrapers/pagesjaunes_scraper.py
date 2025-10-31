"""
Scraper Pages Jaunes pour recherche d'entreprises
"""
import requests
import re
from typing import List, Dict, Optional
from urllib.parse import quote
from utils.logger import setup_logger
from utils.serpapi_client import serpapi_search
import os

logger = setup_logger()


class PagesJaunesScraper:
    """Scraper pour Pages Jaunes"""
    
    def __init__(self):
        """Initialisation du scraper Pages Jaunes"""
        self.base_url = "https://www.pagesjaunes.fr"
        self.serpapi_key = os.getenv('SERPAPI_API_KEY')
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
    
    def search_businesses(self, activity: str, location: str, max_results: int = 10) -> List[Dict]:
        """
        Recherche des entreprises sur Pages Jaunes
        
        Args:
            activity: Type d'activité recherchée
            location: Localisation
            max_results: Nombre maximum de résultats
            
        Returns:
            Liste d'entreprises trouvées avec coordonnées
        """
        # 1) SerpAPI si disponible
        if self.serpapi_key:
            query = f"site:pagesjaunes.fr {activity} {location}"
            logger.info(f"Recherche Pages Jaunes via SerpAPI: {query}")
            items = serpapi_search(query=query, num=max_results, api_key=self.serpapi_key)
            results: List[Dict] = []
            for it in items[:max_results]:
                results.append({
                    'nom': it.get('title'),
                    'description': it.get('snippet'),
                    'url': it.get('link'),
                    'telephone': 'N/A',
                    'email': 'N/A',
                    'adresse': 'N/A',
                    'site_web': it.get('link'),
                    'source': 'pagesjaunes'
                })
            return results

        # 2) Fallback scraping direct
        try:
            # Lazy import to avoid hard dependency when SerpAPI is used
            try:
                from bs4 import BeautifulSoup  # type: ignore
            except Exception as e:
                logger.error(f"beautifulsoup4 manquant pour le fallback Pages Jaunes: {e}")
                return []
            search_url = f"{self.base_url}/recherche/ou={quote(location)}/quoi={quote(activity)}"
            logger.info(f"Recherche Pages Jaunes (fallback scraping): {activity} à {location}")
            response = requests.get(search_url, headers=self.headers)
            response.raise_for_status()
            soup = BeautifulSoup(response.text, 'html.parser')
            businesses = []
            business_items = soup.find_all('div', class_='bi-bloc')[:max_results]
            for item in business_items:
                try:
                    business_data = self._extract_business_data(item)
                    if business_data:
                        businesses.append(business_data)
                        logger.info(f"Entreprise extraite: {business_data.get('nom')}")
                except Exception as e:
                    logger.warning(f"Erreur extraction entreprise: {e}")
                    continue
            logger.info(f"Total de {len(businesses)} entreprises extraites")
            return businesses
        except Exception as e:
            logger.error(f"Erreur lors de la recherche Pages Jaunes: {e}")
            return []
    
    def _extract_business_data(self, item) -> Dict:
        """
        Extrait les données d'une entreprise depuis un élément HTML
        
        Args:
            item: Élément HTML de l'entreprise
            
        Returns:
            Dictionnaire avec les informations de l'entreprise
        """
        business = {}
        
        try:
            # Nom de l'entreprise
            name_elem = item.find('a', class_='denomination-links')
            business['nom'] = name_elem.get_text(strip=True) if name_elem else 'N/A'
            
            # URL de la fiche
            business['url'] = self.base_url + name_elem.get('href') if name_elem and name_elem.get('href') else 'N/A'
            
            # Adresse
            address_elem = item.find('a', class_='adresse')
            business['adresse'] = address_elem.get_text(strip=True) if address_elem else 'N/A'
            
            # Téléphone
            phone_elem = item.find('span', class_='num')
            if phone_elem:
                business['telephone'] = phone_elem.get_text(strip=True)
            else:
                # Chercher dans les attributs data
                phone_data = item.find('a', attrs={'data-phone': True})
                business['telephone'] = phone_data.get('data-phone') if phone_data else 'N/A'
            
            # Email (si disponible sur la fiche détaillée)
            business['email'] = 'N/A'
            
            # Description/Activité
            activity_elem = item.find('div', class_='activite')
            business['description'] = activity_elem.get_text(strip=True) if activity_elem else 'N/A'
            
            # Site web
            website_elem = item.find('a', class_='btn-website')
            business['site_web'] = website_elem.get('href') if website_elem else 'N/A'
            
            business['source'] = 'pagesjaunes'
            
        except Exception as e:
            logger.warning(f"Erreur lors de l'extraction des données: {e}")
        
        return business
    
    def scrape_business_details(self, business_url: str) -> Dict:
        """
        Scrape les détails complets d'une entreprise
        
        Args:
            business_url: URL de l'entreprise
            
        Returns:
            Détails complets de l'entreprise
        """
        try:
            response = requests.get(business_url, headers=self.headers)
            response.raise_for_status()
            
            soup = BeautifulSoup(response.text, 'html.parser')
            
            business = {
                'url': business_url,
                'nom': 'N/A',
                'adresse': 'N/A',
                'telephone': 'N/A',
                'email': 'N/A',
                'site_web': 'N/A',
                'description': 'N/A',
                'horaires': []
            }
            
            # Nom
            name_elem = soup.find('h1', class_='denom')
            if name_elem:
                business['nom'] = name_elem.get_text(strip=True)
            
            # Adresse
            address_elem = soup.find('a', class_='adresse')
            if address_elem:
                business['adresse'] = address_elem.get_text(strip=True)
            
            # Téléphone
            phone_elem = soup.find('span', class_='num')
            if phone_elem:
                business['telephone'] = phone_elem.get_text(strip=True)
            
            # Email
            email_elem = soup.find('a', href=re.compile(r'mailto:'))
            if email_elem:
                business['email'] = email_elem.get('href').replace('mailto:', '')
            
            # Site web
            website_elem = soup.find('a', class_='btn-website')
            if website_elem:
                business['site_web'] = website_elem.get('href')
            
            # Description
            desc_elem = soup.find('div', class_='description')
            if desc_elem:
                business['description'] = desc_elem.get_text(strip=True)
            
            logger.info(f"Détails extraits pour: {business['nom']}")
            return business
            
        except Exception as e:
            logger.error(f"Erreur lors de l'extraction des détails: {e}")
            return {'url': business_url, 'error': str(e)}
    
    def search_professionals(self, profession: str, location: str, max_results: int = 10) -> List[Dict]:
        """
        Recherche de professionnels (profils individuels)
        
        Args:
            profession: Profession recherchée
            location: Localisation
            max_results: Nombre maximum de résultats
            
        Returns:
            Liste de professionnels avec coordonnées
        """
        businesses = self.search_businesses(profession, location, max_results)
        
        # Transformer en format profil
        profiles = []
        for business in businesses:
            profiles.append({
                'nom': business.get('nom'),
                'description': business.get('description'),
                'telephone': business.get('telephone'),
                'email': business.get('email'),
                'adresse': business.get('adresse'),
                'site_web': business.get('site_web'),
                'source': 'pagesjaunes'
            })
        
        return profiles
