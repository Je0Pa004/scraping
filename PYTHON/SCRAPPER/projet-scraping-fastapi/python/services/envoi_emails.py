"""
Service d'envoi d'emails via SendGrid
"""
from typing import List, Dict, Optional
import os
from utils.logger import setup_logger
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail, Email, To, Content


class EmailSender:
    """Service d'envoi d'emails"""
    
    def __init__(self):
        """Initialisation du service d'envoi d'emails"""
        self.api_key = os.getenv('SENDGRID_API_KEY')
        self.from_email = os.getenv('SENDGRID_FROM_EMAIL', 'no-reply@example.com')
        self.client: Optional[SendGridAPIClient] = None
        self.logger = setup_logger()
        
        if not self.api_key:
            self.logger.warning("SENDGRID_API_KEY manquant. L'envoi d'emails échouera.")
        else:
            try:
                self.client = SendGridAPIClient(self.api_key)
                self.logger.info("Client SendGrid initialisé")
            except Exception as e:
                self.logger.error(f"Impossible d'initialiser SendGrid: {e}")
                self.client = None
    
    def send_email(self, to_email: str, subject: str, content: str) -> bool:
        """
        Envoie un email
        
        Args:
            to_email: Adresse email du destinataire
            subject: Sujet de l'email
            content: Contenu de l'email
            
        Returns:
            True si l'envoi a réussi, False sinon
        """
        if not to_email:
            self.logger.error("to_email est requis")
            return False
        if not self.client:
            self.logger.error("Client SendGrid non initialisé")
            return False
        
        try:
            message = Mail(
                from_email=Email(self.from_email),
                to_emails=To(to_email),
                subject=subject or "",
                html_content=Content("text/html", content or "")
            )
            response = self.client.send(message)
            self.logger.info(f"Email envoyé à {to_email} (status {response.status_code})")
            return 200 <= response.status_code < 300
        except Exception as e:
            self.logger.error(f"Erreur d'envoi à {to_email}: {e}")
            return False
    
    def send_bulk_emails(self, recipients: List[Dict]) -> Dict:
        """
        Envoie des emails en masse
        
        Args:
            recipients: Liste de destinataires avec leurs données
            
        Returns:
            Statistiques d'envoi
        """
        stats = {"sent": 0, "failed": 0, "total": len(recipients), "errors": []}
        
        for r in recipients:
            ok = self.send_email(
                to_email=r.get('to_email'),
                subject=r.get('subject', ''),
                content=r.get('content', '')
            )
            if ok:
                stats["sent"] += 1
            else:
                stats["failed"] += 1
                stats["errors"].append({"to": r.get('to_email'), "error": "send_failed"})
        
        self.logger.info(f"Envoi en masse terminé: {stats}")
        return stats
