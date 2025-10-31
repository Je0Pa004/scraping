#!/usr/bin/env python3
"""
Test script pour le scraper LinkedIn
"""
import os
import sys
import json
from dotenv import load_dotenv

# Charger les variables d'environnement
load_dotenv()

# Ajouter le répertoire courant au path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

def test_linkedin_scraper():
    """Test du scraper LinkedIn"""
    try:
        from scrapers.linkedin_scraper import LinkedInScraper
        print("Import du scraper LinkedIn reussi")

        # Initialiser le scraper
        scraper = LinkedInScraper()
        print("Initialisation du scraper LinkedIn reussie")

        # Verifier la configuration
        print(f"Configuration API - Base URL: {scraper.api_base_url}")
        print(f"Configuration API - Token: {'Configure' if scraper.api_token else 'Non configure'}")

        # Test de recherche (avec parametres limites)
        print("\nTest de recherche de profils...")
        try:
            results = scraper.search_profiles(
                titre="Data Scientist",
                localisation="France",
                max_results=3
            )
            print(f"Recherche reussie - {len(results)} profils trouves")

            for i, profile in enumerate(results, 1):
                print(f"   {i}. {profile.get('nom', 'N/A')}")
                if profile.get('email') != 'N/A':
                    print(f"      Email: {profile['email']}")
                if profile.get('profile_url'):
                    print(f"      URL: {profile['profile_url']}")

        except Exception as e:
            print(f"Erreur lors de la recherche: {e}")
            print("   Assurez-vous que LINKEDIN_API_BASE_URL est correctement configure dans .env")

    except ImportError as e:
        print(f"Erreur d'import: {e}")
        print("   Verifiez que toutes les dependances sont installees")
    except Exception as e:
        print(f"Erreur generale: {e}")
        if "Configuration LinkedIn API incomplete" in str(e):
            print("   Vous devez configurer LINKEDIN_API_BASE_URL et LINKEDIN_API_TOKEN dans .env")

if __name__ == "__main__":
    test_linkedin_scraper()
