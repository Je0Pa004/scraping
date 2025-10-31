"""
FastAPI service exposing /scrape to orchestrate existing scrapers.
Runs locally at http://localhost:5000 by starting with: uvicorn api:app --host 0.0.0.0 --port 5000 --reload
"""
from typing import Any, Dict, List, Optional, Set, Tuple
from fastapi import FastAPI, Body
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import time
import random
from utils.logger import setup_logger
from utils.serpapi_client import serpapi_search
from dotenv import load_dotenv

logger = setup_logger()
load_dotenv()  # Charge les variables d'environnement depuis .env si présent

# Lazy import scrapers to keep import errors localized to used sources
from scrapers.linkedin_scraper import LinkedInScraper  # type: ignore
from scrapers.google_scraper import GoogleScraper  # type: ignore
from scrapers.pagesjaunes_scraper import PagesJaunesScraper  # type: ignore


class ScrapeRequest(BaseModel):
    # Source selection: one of linkedin | google | pagesjaunes, or list of them
    source: Optional[str] = Field(default=None, description="linkedin|google|pagesjaunes")
    sources: Optional[List[str]] = None

    # Generic criteria
    titre: Optional[str] = None
    secteur: Optional[str] = None
    localisation: Optional[str] = None
    entreprise: Optional[str] = None
    emploi: Optional[str] = None
    taille_entreprise: Optional[str] = None

    # Google specific
    keywords: Optional[str] = None
    location: Optional[str] = None

    # Pages Jaunes specific
    profession: Optional[str] = None

    # Limit
    max_results: int = Field(default=50, ge=1, le=5000)


class ScrapeResponse(BaseModel):
    results: List[Dict[str, Any]] = []
    count: int = 0


app = FastAPI(title="Scraper API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:4200", "http://127.0.0.1:4200", "http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok"}


def _dedupe(items: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """Deduplicate by (email) or (name+url)."""
    seen: Set[Tuple[str, str]] = set()
    out: List[Dict[str, Any]] = []
    for it in items:
        email = (it.get("email") or "").strip().lower()
        url = (it.get("profile_url") or it.get("url") or it.get("site_web") or "").strip().lower()
        name = (it.get("nom") or it.get("name") or it.get("full_name") or "").strip().lower()
        key = (email if email else name, url)
        if key in seen:
            continue
        seen.add(key)
        out.append(it)
    return out


def _rand_sleep(min_s: float = 0.5, max_s: float = 2.0) -> None:
    try:
        time.sleep(random.uniform(min_s, max_s))
    except Exception:
        pass


def _collect_serpapi(query: str, target: int, engine: str = "google") -> List[Dict[str, Any]]:
    collected: List[Dict[str, Any]] = []
    start = 0
    while len(collected) < target:
        page_size = min(100, target - len(collected))
        items = serpapi_search(query=query, num=page_size, engine=engine, start=start)
        if not items:
            break
        collected.extend(items)
        start += page_size
        _rand_sleep(0.3, 0.7)
    return collected


@app.post("/scrape", response_model=ScrapeResponse)
def scrape(req: ScrapeRequest = Body(...)) -> ScrapeResponse:
    logger.info(f"/scrape called with: {req.dict()}")
    sources = req.sources or ([req.source] if req.source else ["google"])  # default google

    results: List[Dict[str, Any]] = []

    for src in sources:
        src_l = (src or "").strip().lower()
        try:
            if src_l == "linkedin":
                kw = (req.titre or req.emploi or "").strip()
                loc = (req.localisation or req.location or "").strip()
                query = f"site:linkedin.com/in {kw} {loc}".strip()
                serp_items = _collect_serpapi(query=query, target=req.max_results, engine="google")
                batch = []
                for it in serp_items:
                    batch.append({
                        "nom": it.get("title") or "N/A",
                        "description": it.get("snippet") or "N/A",
                        "email": "N/A",
                        "telephone": "N/A",
                        "profile_url": it.get("link"),
                        "source": "linkedin",
                    })
                results.extend(batch[: req.max_results])

            elif src_l == "google":
                scraper = GoogleScraper()
                kw = req.keywords or req.titre or req.emploi or ""
                batch = scraper.search_profiles(keywords=kw, location=req.localisation or req.location)
                for b in batch[: req.max_results]:
                    b.setdefault("profile_url", b.get("url"))
                    b.setdefault("source", "google")
                if len(batch) < req.max_results:
                    needed = req.max_results - len(batch)
                    q = f"{kw} {(req.localisation or req.location or '').strip()}".strip()
                    serp_items = _collect_serpapi(query=q, target=needed, engine="google")
                    extra = []
                    for it in serp_items:
                        extra.append({
                            "nom": it.get("title") or "N/A",
                            "description": it.get("snippet") or "N/A",
                            "email": "N/A",
                            "telephone": "N/A",
                            "profile_url": it.get("link"),
                            "source": "google",
                        })
                    batch.extend(extra)
                results.extend(batch[: req.max_results])

            elif src_l == "google_maps":
                kw = (req.keywords or req.titre or req.emploi or "").strip()
                loc = (req.localisation or req.location or "").strip()
                q = f"{kw} {loc}".strip()
                serp_items = _collect_serpapi(query=q, target=req.max_results, engine="google_maps")
                batch = []
                for it in serp_items:
                    batch.append({
                        "nom": it.get("title") or "N/A",
                        "description": it.get("snippet") or "N/A",
                        "email": "N/A",
                        "telephone": it.get("phone") or "N/A",
                        "profile_url": it.get("link"),
                        "source": "google_maps",
                    })
                results.extend(batch[: req.max_results])

            elif src_l in ("pagesjaunes", "pages_jaunes", "pages-jaunes"):
                scraper = PagesJaunesScraper()
                activity = req.profession or req.titre or req.emploi or (req.secteur or "")
                location = req.localisation or req.location or ""
                batch = scraper.search_professionals(profession=activity, location=location, max_results=req.max_results)
                for b in batch:
                    b.setdefault("source", "pagesjaunes")
                    b.setdefault("profile_url", b.get("site_web"))
                results.extend(batch)

            # Random gentle delay between sources to reduce burst
            _rand_sleep(0.8, 2.2)

        except Exception as e:
            logger.exception(f"Error while scraping source={src}: {e}")
            # continue to next source
            continue

    # Cleanup and limit
    results = _dedupe(results)
    if len(results) > req.max_results:
        results = results[: req.max_results]

    logger.info(f"/scrape returning {len(results)} results")
    return ScrapeResponse(results=results, count=len(results))
