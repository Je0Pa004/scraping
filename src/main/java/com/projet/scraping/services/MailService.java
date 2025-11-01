package com.projet.scraping.services;

import com.projet.scraping.entities.Candidat;

public interface MailService {
    void sendInvitation(Candidat candidat, String replyToAddress);
}
