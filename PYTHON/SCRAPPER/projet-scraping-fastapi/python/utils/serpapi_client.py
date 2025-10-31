"""
Client utilitaire pour SerpAPI (Google Search)
"""
import os
import requests
from typing import List, Dict, Optional


def serpapi_search(query: str, num: int = 10, api_key: Optional[str] = None, hl: str = "fr", start: int = 0, engine: str = "google", params_extra: Optional[Dict] = None) -> List[Dict]:
    """
    Effectue une recherche via SerpAPI et retourne la liste des résultats organiques.

    Args:
        query: Requête Google
        num: Nombre de résultats souhaités (max ~100 selon SerpAPI)
        api_key: Clé API SerpAPI. Si None, lit SERPAPI_API_KEY depuis l'env
        hl: Langue (fr par défaut)
        start: Décalage de départ (pagination Google: 0, 10, 20, ...)
        engine: Moteur SerpAPI ("google", "google_maps", ...)
        params_extra: Paramètres additionnels passés à SerpAPI

    Returns:
        Liste d'objets résultat (titre, lien, description selon SerpAPI)
    """
    key = api_key or os.getenv("SERPAPI_API_KEY")
    if not key:
        return []

    url = "https://serpapi.com/search.json"
    params = {
        "q": query,
        "num": num,
        "hl": hl,
        "api_key": key,
        "engine": engine or "google",
        "start": start,
    }
    if params_extra:
        params.update(params_extra)

    try:
        resp = requests.get(url, params=params, timeout=30)
        resp.raise_for_status()
        data = resp.json() or {}
        if (engine or "google") == "google_maps":
            # Résultats Google Maps
            items = data.get("local_results", [])
            normalized = []
            for it in items:
                normalized.append({
                    "title": it.get("title"),
                    "link": it.get("link") or it.get("website"),
                    "snippet": it.get("type") or it.get("address"),
                    "phone": it.get("phone"),
                    "source": "serpapi_maps"
                })
            return normalized
        else:
            # Résultats Google classiques
            items = data.get("organic_results", [])
            normalized = []
            for it in items:
                normalized.append({
                    "title": it.get("title"),
                    "link": it.get("link"),
                    "snippet": it.get("snippet"),
                    "source": "serpapi"
                })
            return normalized
    except Exception:
        return []
