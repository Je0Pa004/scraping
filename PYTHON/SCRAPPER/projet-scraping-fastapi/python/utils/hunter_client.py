"""
Client utilitaire pour Hunter.io
"""
import os
import requests
from typing import Optional, Dict

HUNTER_API_URL = "https://api.hunter.io/v2"


def _api_key() -> Optional[str]:
    return os.getenv("HUNTER_API_KEY")


def find_domain(company: str) -> Optional[str]:
    """
    Utilise Hunter Domain Search pour deviner le domaine d'une entreprise à partir de son nom.
    """
    key = _api_key()
    if not key or not company:
        return None
    try:
        resp = requests.get(
            f"{HUNTER_API_URL}/domain-search",
            params={"company": company, "api_key": key, "limit": 1},
            timeout=15,
        )
        resp.raise_for_status()
        data = resp.json() or {}
        domain = data.get("data", {}).get("domain")
        return domain
    except Exception:
        return None


def find_email(first_name: str, last_name: str, domain: str) -> Optional[Dict]:
    """
    Utilise Hunter Email Finder pour trouver un email à partir du prénom, nom et domaine.
    Retourne {email, score} si trouvé.
    """
    key = _api_key()
    if not key or not (first_name and last_name and domain):
        return None
    try:
        resp = requests.get(
            f"{HUNTER_API_URL}/email-finder",
            params={
                "first_name": first_name,
                "last_name": last_name,
                "domain": domain,
                "api_key": key,
            },
            timeout=15,
        )
        resp.raise_for_status()
        data = resp.json() or {}
        email = data.get("data", {}).get("email")
        score = data.get("data", {}).get("score")
        if email:
            return {"email": email, "score": score}
        return None
    except Exception:
        return None
