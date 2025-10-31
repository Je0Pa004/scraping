"""
Configuration du logging personnalisé
"""
import os
import sys
from loguru import logger


def setup_logger(log_level: str = None):
    """
    Configure le logger pour l'application
    
    Args:
        log_level: Niveau de log (DEBUG, INFO, WARNING, ERROR)
    """
    if log_level is None:
        log_level = os.getenv('LOG_LEVEL', 'INFO')
    
    # Configuration du logger
    logger.remove()  # Supprime le handler par défaut
    
    # Handler pour la console
    logger.add(
        sys.stdout,
        format="<green>{time:YYYY-MM-DD HH:mm:ss}</green> | <level>{level: <8}</level> | <cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - <level>{message}</level>",
        level=log_level
    )
    
    # Handler pour les fichiers
    logger.add(
        "logs/app_{time:YYYY-MM-DD}.log",
        rotation="00:00",
        retention="30 days",
        level=log_level,
        format="{time:YYYY-MM-DD HH:mm:ss} | {level: <8} | {name}:{function}:{line} - {message}"
    )
    
    return logger
