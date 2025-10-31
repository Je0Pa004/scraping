"""
Enrichissement interne des contacts (emails / téléphones) sans services tiers payants.
- Recherche web via SerpAPI (Google) si clé présente
- Récupère les premières pages non-LinkedIn et applique des regex sur le HTML brut
"""
from typing import Optional, Tuple, List, Set
import re
import requests
from utils.serpapi_client import serpapi_search

EMAIL_REGEX = re.compile(r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b")
PHONE_REGEX_FR = re.compile(r"(?:\+33|0)[1-9](?:[\s\.-]?\d{2}){4}")
DOMAIN_REGEX = re.compile(r"\b(?:[a-z0-9-]+\.)+[a-z]{2,}\b", re.I)

IGNORED_EMAIL_DOMAINS: Set[str] = {
    'sentry.io', 'amznses.com', 'amazonses.com', 'example.com', 'mailinator.com',
    'noreply.com', 'no-reply.com', 'donotreply.com', 'reply.github.com'
}


def _fetch_html(url: str, timeout: int = 10) -> Optional[str]:
    try:
        r = requests.get(url, timeout=timeout, headers={
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })
        if r.status_code >= 400:
            return None
        return r.text
    except Exception:
        return None


def _extract_contacts_from_html(html: str) -> Tuple[Optional[str], Optional[str]]:
    if not html:
        return (None, None)
    email = None
    phone = None
    m = EMAIL_REGEX.search(html)
    if m:
        email = m.group(0)
    m2 = PHONE_REGEX_FR.search(html)
    if m2:
        phone = m2.group(0)
    return (email, phone)


def _extract_domains_from_html(html: str) -> List[str]:
    if not html:
        return []
    domains = set(DOMAIN_REGEX.findall(html))
    # filtrer quelques domaines génériques peu utiles
    bad_suffixes = {'.png', '.jpg', '.jpeg', '.gif', '.svg'}
    out = []
    for d in domains:
        if any(d.endswith(s) for s in bad_suffixes):
            continue
        # ignorer sous-domaines très techniques (cdn, assets)
        if any(part in d for part in ['cdn.', 'static.', 'assets.', 'fonts.']):
            continue
        out.append(d.lower())
    return out


def _is_valid_email(addr: str) -> bool:
    if not addr or '@' not in addr:
        return False
    domain = addr.split('@', 1)[1].lower()
    if domain in IGNORED_EMAIL_DOMAINS:
        return False
    # ignorer les adresses no-reply
    local = addr.split('@', 1)[0].lower()
    if any(tag in local for tag in ['noreply', 'no-reply', 'donotreply', 'do-not-reply']):
        return False
    return True


def _normalize_phone(phone: Optional[str]) -> Optional[str]:
    if not phone:
        return None
    digits = re.sub(r"[^0-9+]", "", phone)
    # Convertir 0X en +33X si FR
    if digits.startswith('0') and len(digits) >= 10:
        return '+33' + digits[1:]
    return digits


def _split_name_tokens(name: str) -> Tuple[Optional[str], Optional[str]]:
    if not name:
        return (None, None)
    # couper sur séparateurs usuels
    for sep in [' - ', ' | ', '–']:
        if sep in name:
            name = name.split(sep, 1)[0]
    parts = [p for p in name.strip().split() if p.isalpha()]
    if len(parts) >= 2:
        return (parts[0].lower(), parts[-1].lower())
    return (None, None)


def _generate_email_candidates(first: str, last: str, domain: str) -> List[str]:
    candidates = []
    fi = first[0] if first else ''
    li = last[0] if last else ''
    patterns = [
        f"{first}.{last}@{domain}",
        f"{first}{last}@{domain}",
        f"{fi}{last}@{domain}",
        f"{first}{li}@{domain}",
        f"{last}.{first}@{domain}",
    ]
    return [p.replace('..', '.').lower() for p in patterns]


def enrich_contacts_via_web(name: str, description: Optional[str], max_results: int = 5) -> Tuple[Optional[str], Optional[str]]:
    """
    Tente d'enrichir email et téléphone en recherchant sur le web (SerpAPI) d'autres pages
    liées au nom + contexte, puis en appliquant des regex sur les pages trouvées.

    Args:
        name: Nom complet
        description: Contexte (titre, entreprise, etc.)
        max_results: Nombre de résultats web à tester

    Returns:
        (email, phone) si trouvés, sinon (None, None)
    """
    query_parts: List[str] = []
    if name:
        query_parts.append(f'"{name}"')
    if description:
        query_parts.append(description)
    query = " ".join(query_parts) if query_parts else name
    if not query:
        return (None, None)

    # Deux passes: requête de base puis requête + ' email'
    queries = [query, f"{query} email" if query else "email"]

    for q in queries:
        items = serpapi_search(query=q, num=max_results)
        if not items:
            continue
        tested = 0
        for it in items:
            if tested >= max_results:
                break
            url = it.get('link') or it.get('url')
            if not url:
                continue
            if 'linkedin.com' in url:
                continue
            html = _fetch_html(url)
            if not html:
                continue
            email, phone = _extract_contacts_from_html(html)
            if email and _is_valid_email(email):
                return (email, _normalize_phone(phone))
            # Si pas d'email direct, tenter de générer à partir d'un domaine trouvé
            first, last = _split_name_tokens(name)
            if first and last:
                domains = _extract_domains_from_html(html)
                for domain in domains:
                    for candidate in _generate_email_candidates(first, last, domain):
                        if candidate in html and _is_valid_email(candidate):
                            return (candidate, _normalize_phone(phone))
            # Sinon renvoyer au moins un téléphone si trouvé
            if phone:
                return (None, _normalize_phone(phone))
            tested += 1

    return (None, None)
