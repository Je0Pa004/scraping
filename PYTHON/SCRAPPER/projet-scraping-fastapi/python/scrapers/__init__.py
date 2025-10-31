"""
Module de scrapers
"""
from importlib import import_module

__all__ = ['LinkedInScraper', 'GoogleScraper', 'PagesJaunesScraper']


def __getattr__(name):
    if name == 'LinkedInScraper':
        return import_module('.linkedin_scraper', __name__).__dict__['LinkedInScraper']
    if name == 'GoogleScraper':
        return import_module('.google_scraper', __name__).__dict__['GoogleScraper']
    if name == 'PagesJaunesScraper':
        return import_module('.pagesjaunes_scraper', __name__).__dict__['PagesJaunesScraper']
    raise AttributeError(name)
