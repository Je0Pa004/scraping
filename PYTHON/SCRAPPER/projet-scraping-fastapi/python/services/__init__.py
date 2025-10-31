"""
Module de services IA
"""
from .selection_profils import ProfileSelector
from .envoi_emails import EmailSender
from .analyse_cv import CVAnalyzer

__all__ = ['ProfileSelector', 'EmailSender', 'CVAnalyzer']
