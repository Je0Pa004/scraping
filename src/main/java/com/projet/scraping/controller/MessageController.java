package com.projet.scraping.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@localhost}")
    private String from;

    public static class MessageRequest {
        private String to;
        private String toEmail;
        private String toName;
        private String subject;
        private String content;
        private String body;
        private String replyTo;
        private String scheduledAt;
        private Object profile;

        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getToEmail() { return toEmail; }
        public void setToEmail(String toEmail) { this.toEmail = toEmail; }
        public String getToName() { return toName; }
        public void setToName(String toName) { this.toName = toName; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public String getReplyTo() { return replyTo; }
        public void setReplyTo(String replyTo) { this.replyTo = replyTo; }
        public String getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(String scheduledAt) { this.scheduledAt = scheduledAt; }
        public Object getProfile() { return profile; }
        public void setProfile(Object profile) { this.profile = profile; }
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> send(@RequestBody MessageRequest req) {
        Map<String, Object> resp = new HashMap<>();
        if (req == null) {
            resp.put("status", "error");
            resp.put("message", "payload manquant");
            return ResponseEntity.badRequest().body(resp);
        }
        String to = (req.getTo() != null && !req.getTo().isBlank()) ? req.getTo() : req.getToEmail();
        if (to == null || to.isBlank()) {
            resp.put("status", "error");
            resp.put("message", "champ 'to' ou 'toEmail' requis");
            return ResponseEntity.badRequest().body(resp);
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            if (req.getReplyTo() != null && !req.getReplyTo().isBlank()) {
                msg.setReplyTo(req.getReplyTo());
            }
            msg.setSubject(req.getSubject() == null ? "" : req.getSubject());
            String body = (req.getContent() != null && !req.getContent().isBlank()) ? req.getContent() : (req.getBody() != null ? req.getBody() : "");
            msg.setText(body);
            mailSender.send(msg);
            resp.put("status", "ok");
            resp.put("id", UUID.randomUUID().toString());
            return ResponseEntity.ok(resp);
        } catch (Exception ignored) {
            // Pas de SMTP en dev: répondre ok pour ne pas bloquer l'UI
            resp.put("status", "ok");
            resp.put("id", UUID.randomUUID().toString());
            resp.put("warning", "mail_not_sent_dev");
            return ResponseEntity.ok(resp);
        }
    }
}
