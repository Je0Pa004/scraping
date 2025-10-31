#!/usr/bin/env python3
"""
Test script pour le nouveau scraper LinkedIn avec linkedin-api
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
    """Test du scraper LinkedIn avec token Bearer"""
    print("=== Test du Scraper LinkedIn (Token Bearer) ===")

    try:
        from scrapers.linkedin_scraper_new import LinkedInScraper
        print("Import du scraper LinkedIn reussi")

        # Initialiser le scraper
        scraper = LinkedInScraper()
        print("Initialisation du scraper LinkedIn reussie")

        # Verifier la configuration
        print(f"Configuration - Token: {'Configure' if scraper.api_token else 'Non configure'}")
        print(f"Configuration - Base URL: {scraper.api_base_url}")

        # Test de recherche (avec parametres limites)
        print("\nTest de recherche de profils...")
        try:
            results = scraper.search_profiles(
                titre="Data Scientist",
                localisation="France",
                max_results=2  # Limité pour le test
            )
            print(f"Recherche reussie - {len(results)} profils trouves")

            for i, profile in enumerate(results, 1):
                print(f"\n   Profil {i}:")
                print(f"   Nom: {profile.get('nom', 'N/A')}")
                print(f"   Titre: {profile.get('description', 'N/A')}")
                print(f"   Localisation: {profile.get('localisation', 'N/A')}")
                print(f"   URL: {profile.get('profile_url', 'N/A')}")

        except Exception as e:
            print(f"Erreur lors de la recherche: {e}")
            print("   Verifiez votre token LinkedIn dans le fichier .env")

    except ImportError as e:
        print(f"Erreur d'import: {e}")
        print("   Assurez-vous que requests est installe: pip install requests")
    except Exception as e:
        print(f"Erreur generale: {e}")
        if "Token LinkedIn requis" in str(e):
            print("   Vous devez configurer LINKEDIN_API_TOKEN dans .env")

def test_linkedin_token():
    """Test de validité du token LinkedIn"""
    print("\n=== Test du Token LinkedIn ===")

    try:
        import os
        import requests

        token = os.getenv('LINKEDIN_API_TOKEN')
        if not token:
            print("Token LinkedIn non configure")
            return

        print(f"Token trouve (longueur: {len(token)})")

        # Test basique du token avec une requête simple
        headers = {
            'Authorization': f'Bearer {token}',
            'Content-Type': 'application/json',
            'X-Restli-Protocol-Version': '2.0.0'
        }

        # Test avec une requête simple (récupérer son propre profil)
        response = requests.get(
            'https://api.linkedin.com/v2/people/~',
            headers=headers,
            timeout=10
        )

        if response.status_code == 200:
            print("Token valide - Acces autorise")
            data = response.json()
            nom = f"{data.get('firstName', {}).get('localized', {}).get('fr_FR', '')} {data.get('lastName', {}).get('localized', {}).get('fr_FR', '')}"
            print(f"Profil accessible: {nom}")
        else:
            print(f"Token invalide ou expire - Status: {response.status_code}")
            print(f"   Reponse: {response.text[:200]}...")

    except Exception as e:
        print(f"Erreur lors du test du token: {e}")

if __name__ == "__main__":
    test_linkedin_token()
    print("\n" + "="*50)
    test_linkedin_scraper()
