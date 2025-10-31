"""
Point d'entrée principal du module Python
Lancé par Spring via ProcessBuilder
"""
import sys
import argparse
import json
from dotenv import load_dotenv
from utils.logger import setup_logger

# Chargement des variables d'environnement
load_dotenv()
logger = setup_logger()

# Mapping actions → classes scrapers/services
SCRAPER_CLASSES = {
    'scrape_linkedin': ('scrapers.linkedin_scraper', 'LinkedInScraper'),
    'scrape_google': ('scrapers.google_scraper', 'GoogleScraper'),
    'scrape_pagesjaunes': ('scrapers.pagesjaunes_scraper', 'PagesJaunesScraper'),
}

SERVICE_CLASSES = {
    'select_profiles': ('services.selection_profils', 'ProfileSelector'),
    'send_emails': ('services.envoi_emails', 'EmailSender'),
    'analyze_cv': ('services.analyse_cv', 'CVAnalyzer'),
}


def load_class(module_path: str, class_name: str):
    """Charge dynamiquement une classe depuis un module"""
    try:
        module = __import__(module_path, fromlist=[class_name])
        return getattr(module, class_name)
    except ImportError as e:
        logger.error(f"Dépendance manquante pour {class_name}: {e}")
        raise RuntimeError(f"Veuillez installer la dépendance nécessaire pour {class_name}. Détails: {e}")


def main():
    """Point d'entrée principal"""
    parser = argparse.ArgumentParser(description='Module de scraping et IA')
    parser.add_argument('--action', type=str, required=True,
                        choices=list(SCRAPER_CLASSES.keys()) + list(SERVICE_CLASSES.keys()),
                        help='Action à exécuter')
    parser.add_argument('--params', type=str, help='Paramètres JSON pour l\'action')

    args = parser.parse_args()
    params = {}
    if args.params:
        try:
            params = json.loads(args.params)
        except json.JSONDecodeError:
            logger.error("Paramètres JSON invalides fournis à --params")
            print(json.dumps({"error": "Invalid JSON for --params"}, ensure_ascii=False))
            return 1

    logger.info(f"Action demandée: {args.action}")
    logger.info(f"Paramètres: {params}")

    try:
        # Scrapers
        if args.action in SCRAPER_CLASSES:
            module_path, class_name = SCRAPER_CLASSES[args.action]
            ScraperClass = load_class(module_path, class_name)
            scraper = ScraperClass()

            if args.action == 'scrape_linkedin':
                results = scraper.search_profiles(
                    titre=params.get('titre'),
                    secteur=params.get('secteur'),
                    localisation=params.get('localisation'),
                    entreprise=params.get('entreprise'),
                    emploi=params.get('emploi'),
                    taille_entreprise=params.get('taille_entreprise'),
                    max_results=int(params.get('max_results', 10))
                )

            elif args.action == 'scrape_google':
                results = scraper.search_profiles(
                    keywords=params.get('keywords', ''),
                    location=params.get('location')
                )

            elif args.action == 'scrape_pagesjaunes':
                results = scraper.search_professionals(
                    profession=params.get('profession') or params.get('activity', ''),
                    location=params.get('location', ''),
                    max_results=int(params.get('max_results', 10))
                )

            print(json.dumps({"results": results}, ensure_ascii=False))

        # Services
        elif args.action in SERVICE_CLASSES:
            module_path, class_name = SERVICE_CLASSES[args.action]
            ServiceClass = load_class(module_path, class_name)
            service = ServiceClass()

            if args.action == 'select_profiles':
                profiles = params.get('profiles', [])
                criteria = params.get('criteria', {})
                results = service.select_relevant_profiles(profiles, criteria)

            elif args.action == 'send_emails':
                if 'recipients' in params:
                    results = service.send_bulk_emails(params['recipients'])
                else:
                    ok = service.send_email(
                        to_email=params.get('to_email'),
                        subject=params.get('subject', ''),
                        content=params.get('content', '')
                    )
                    results = {"result": ok}

            elif args.action == 'analyze_cv':
                cv_text = params.get('cv_text', '')
                results = service.analyze_cv(cv_text)

            print(json.dumps({"results": results}, ensure_ascii=False))

        else:
            print(json.dumps({"error": "Action non supportée"}, ensure_ascii=False))
            return 1

        return 0

    except Exception as e:
        logger.exception("Erreur lors de l'exécution de l'action")
        print(json.dumps({"error": str(e)}, ensure_ascii=False))
        return 1


if __name__ == "__main__":
    sys.exit(main())
