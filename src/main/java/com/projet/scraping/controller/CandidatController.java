package com.projet.scraping.controller;

import com.projet.scraping.DtoRequest.InvitationRequest;
import com.projet.scraping.entities.Candidat;
import com.projet.scraping.services.CandidatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CandidatController {

    private final CandidatService service;

    // Recruteur: créer une invitation (enverra un email côté service ou via un MailService futur)
    @PostMapping("/candidats/invitations")
    public ResponseEntity<Candidat> inviter(@RequestBody InvitationRequest req) {
        return ResponseEntity.ok(service.inviter(req));
    }

    // Recruteur: lister les candidats
    @GetMapping("/candidats")
    public ResponseEntity<List<Candidat>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Public: upload de CV par token (fallback au flux Reply-To)
    @PostMapping("/public/candidats/upload-cv")
    public ResponseEntity<Candidat> uploadCv(@RequestParam("token") String token, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadCv(token, file));
    }

    // Webhook provider inbound: extrait le token depuis l'adresse 'to' (format plus-addressing cv+token@domain)
    // et accepte le fichier sous 'file' ou 'attachment'
    @PostMapping("/public/mail-inbound")
    public ResponseEntity<Candidat> inbound(
            @RequestParam("to") String to,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment
    ) {
        String token = extractTokenFromAddress(to);
        MultipartFile f = file != null ? file : attachment;
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (f == null || f.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.uploadCv(token, f));
    }

    private String extractTokenFromAddress(String to) {
        if (to == null) return null;
        // supporte formats: "cv+TOKEN@domain" ou liste séparée par virgules
        String addr = to.split(",")[0].trim();
        int plus = addr.indexOf('+');
        int at = addr.indexOf('@');
        if (plus >= 0 && at > plus) {
            return addr.substring(plus + 1, at);
        }
        return null;
    }
}
