package com.projet.scraping.services.implementation;

import com.projet.scraping.entities.Candidat;
import com.projet.scraping.services.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@localhost}")
    private String from;

    @Override
    public void sendInvitation(Candidat candidat, String replyToAddress) {
        if (candidat == null || candidat.getEmail() == null) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(candidat.getEmail());
            if (replyToAddress != null && !replyToAddress.isBlank()) {
                msg.setReplyTo(replyToAddress);
            }
            msg.setSubject("Invitation à déposer votre CV");
            msg.setText("Bonjour " + (candidat.getNom() != null ? candidat.getNom() : "") + ",\n\n"
                    + "Merci de répondre à ce message en joignant votre CV au format PDF.\n"
                    + "Votre candidature sera traitée automatiquement.\n\n"
                    + "Cordialement,");
            mailSender.send(msg);
        } catch (Exception ignored) {
            // En environnement dev sans SMTP configuré, ignorer l'erreur
        }
    }
}
