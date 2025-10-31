"""
Scraper Google pour recherche d'informations
"""
import os
import requests
import re
from typing import List, Dict, Optional, Any
from utils.logger import setup_logger
from utils.serpapi_client import serpapi_search
from utils.contact_enricher import enrich_contacts_via_web

logger = setup_logger()


class GoogleScraper:
    """Scraper pour Google Search"""
    
    def __init__(self):
        """Initialisation du scraper Google"""
        self.api_key = os.getenv('GOOGLE_API_KEY')
        self.cse_id = os.getenv('GOOGLE_CSE_ID')
        self.serpapi_key = os.getenv('SERPAPI_API_KEY')
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
    
    def search(self, query: str, num_results: int = 10) -> List[Dict]:
        """
        Effectue une recherche Google
        
        Args:
            query: Requête de recherche
            num_results: Nombre de résultats souhaités
            
        Returns:
            Liste de résultats de recherche
        """
        try:
            results = []

            # 1) SerpAPI si disponible
            if self.serpapi_key:
                serp_items = serpapi_search(query=query, num=num_results, api_key=self.serpapi_key)
                for it in serp_items[:num_results]:
                    results.append({
                        'titre': it.get('title'),
                        'url': it.get('link'),
                        'description': it.get('snippet'),
                        'source': 'serpapi'
                    })
                return results

            # 2) Google CSE
            if self.api_key and self.cse_id:
                results = self._search_with_api(query, num_results)
            else:
                # 3) Fallback scraping direct
                results = self._search_with_scraping(query, num_results)
            
            logger.info(f"Recherche Google effectuée: {len(results)} résultats pour '{query}'")
            return results
            
        except Exception as e:
            logger.error(f"Erreur lors de la recherche Google: {e}")
            return []
    
    def _search_with_api(self, query: str, num_results: int) -> List[Dict]:
        """Recherche via l'API Google Custom Search"""
        results = []
        
        try:
            url = "https://www.googleapis.com/customsearch/v1"
            params = {
                'key': self.api_key,
                'cx': self.cse_id,
                'q': query,
                'num': min(num_results, 10)
            }
            
            response = requests.get(url, params=params)
            response.raise_for_status()
            data = response.json()
            
            for item in data.get('items', []):
                results.append({
                    'titre': item.get('title'),
                    'url': item.get('link'),
                    'description': item.get('snippet'),
                    'source': 'google'
                })
                
        except Exception as e:
            logger.error(f"Erreur API Google: {e}")
        
        return results
    
    def _search_with_scraping(self, query: str, num_results: int) -> List[Dict]:
        """Recherche par scraping direct (fallback)"""
        results = []
        
        try:
            # Lazy import to avoid hard dependency when not needed
            try:
                from bs4 import BeautifulSoup  # type: ignore
            except Exception as e:
                logger.error(f"beautifulsoup4 manquant pour le fallback scraping: {e}")
                return []
            search_url = f"https://www.google.com/search?q={query}&num={num_results}"
            response = requests.get(search_url, headers=self.headers)
            response.raise_for_status()
            
            soup = BeautifulSoup(response.text, 'html.parser')
            search_results = soup.find_all('div', class_='g')
            
            for result in search_results[:num_results]:
                try:
                    title_elem = result.find('h3')
                    link_elem = result.find('a')
                    snippet_elem = result.find('div', class_='VwiC3b')
                    
                    if title_elem and link_elem:
                        results.append({
                            'titre': title_elem.get_text(),
                            'url': link_elem.get('href'),
                            'description': snippet_elem.get_text() if snippet_elem else '',
                            'source': 'google'
                        })
                except Exception as e:
                    logger.warning(f"Erreur extraction résultat: {e}")
                    continue
                    
        except Exception as e:
            logger.error(f"Erreur scraping Google: {e}")
        
        return results
    
    def scrape_page(self, url: str) -> Dict:
        """
        Scrape le contenu d'une page web
        
        Args:
            url: URL de la page à scraper
            
        Returns:
            Contenu de la page
        """
        try:
            # Lazy import to avoid requiring bs4 when SerpAPI is used
            try:
                from bs4 import BeautifulSoup  # type: ignore
            except Exception as e:
                logger.error(f"beautifulsoup4 manquant pour scrape_page: {e}")
                return {'url': url, 'error': 'bs4_not_installed'}
            response = requests.get(url, headers=self.headers, timeout=10)
            response.raise_for_status()
            soup = BeautifulSoup(response.text, 'html.parser')
            
            # Extraction des informations de contact
            page_data = {
                'url': url,
                'titre': soup.title.string if soup.title else '',
                'emails': self._extract_emails(soup),
                'telephones': self._extract_phones(soup),
                'texte': soup.get_text()[:1000]  # Premier 1000 caractères
            }
            
            logger.info(f"Page scrapée: {url}")
            return page_data
            
        except Exception as e:
            logger.error(f"Erreur lors du scraping de {url}: {e}")
            return {'url': url, 'error': str(e)}
    
    def _extract_emails(self, soup: Any) -> List[str]:
        """Extrait les emails d'une page"""
        text = soup.get_text()
        email_pattern = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'
        emails = list(set(re.findall(email_pattern, text)))
        return emails
    
    def _extract_phones(self, soup: Any) -> List[str]:
        """Extrait les numéros de téléphone d'une page"""
        text = soup.get_text()
        phone_pattern = r'(?:\+33|0)[1-9](?:[\s.-]?\d{2}){4}'
        phones = list(set(re.findall(phone_pattern, text)))
        return phones
    
    def search_profiles(self, keywords: str, location: str = None) -> List[Dict]:
        """
        Recherche de profils professionnels sur Google
        
        Args:
            keywords: Mots-clés de recherche
            location: Localisation optionnelle
            
        Returns:
            Liste de profils trouvés
        """
        query = f"{keywords} profil professionnel"
        if location:
            query += f" {location}"
        
        search_results = self.search(query, num_results=20)
        profiles: List[Dict] = []

        for result in search_results:
            # Si les résultats viennent de SerpAPI, enrichir via web (regex) sans bs4
            if result.get('source') == 'serpapi':
                email, phone = (None, None)
                try:
                    email, phone = enrich_contacts_via_web(
                        name=result.get('titre') or '',
                        description=result.get('description') or '',
                        max_results=5
                    )
                except Exception:
                    pass
                profiles.append({
                    'nom': result.get('titre'),
                    'description': result.get('description'),
                    'url': result.get('url'),
                    'email': email or 'N/A',
                    'telephone': phone or 'N/A',
                    'source': 'google'
                })
                continue

            # Sinon, tenter de parser la page pour extraire les contacts
            page_data = self.scrape_page(result['url'])
            if page_data.get('emails') or page_data.get('telephones'):
                profiles.append({
                    'nom': result.get('titre'),
                    'description': result.get('description'),
                    'url': result.get('url'),
                    'email': page_data.get('emails', ['N/A'])[0] if page_data.get('emails') else 'N/A',
                    'telephone': page_data.get('telephones', ['N/A'])[0] if page_data.get('telephones') else 'N/A',
                    'source': 'google'
                })
        
        return profiles
