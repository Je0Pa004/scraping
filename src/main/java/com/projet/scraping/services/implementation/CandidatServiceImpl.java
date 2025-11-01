package com.projet.scraping.services.implementation;

import com.projet.scraping.DtoRequest.InvitationRequest;
import com.projet.scraping.entities.Candidat;
import com.projet.scraping.entities.enums.StatutCandidat;
import com.projet.scraping.repositories.CandidatRepository;
import com.projet.scraping.services.CandidatService;
import com.projet.scraping.ws.WsBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CandidatServiceImpl implements CandidatService {

    private final CandidatRepository repo;
    private final WsBroadcaster ws;
    private final com.projet.scraping.services.MailService mailService;

    @Value("${app.cv.storageDir:uploads}")
    private String storageDir;

    @Value("${app.cv.replyToBase:cv@localhost}")
    private String replyToBase;

    @Override
    public Candidat inviter(InvitationRequest req) {
        Objects.requireNonNull(req.getEmail(), "email requis");
        Candidat c = new Candidat();
        c.setPublicId(UUID.randomUUID());
        c.setEmail(req.getEmail().trim());
        c.setNom(opt(req.getNom()));
        c.setTelephone(opt(req.getTelephone()));
        c.setProfilPublicId(opt(req.getProfilPublicId()));
        c.setNotes(opt(req.getNotes()));
        c.setStatut(StatutCandidat.INVITE);
        c.setDateInvitation(LocalDateTime.now());
        String token = UUID.randomUUID().toString().replace("-", "");
        c.setTokenInvite(token);
        c.setTokenExpireAt(LocalDateTime.now().plusDays(14));
        c = repo.save(c);

        // Envoi email d'invitation avec Reply-To
        String replyTo = buildReplyTo(token);
        mailService.sendInvitation(c, replyTo);

        return c;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Candidat> getAll() {
        return repo.findAll();
    }

    @Override
    public Candidat uploadCv(String token, MultipartFile file) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("token manquant");
        Candidat c = repo.findByTokenInvite(token).orElseThrow(() -> new NoSuchElementException("Candidat introuvable"));
        if (c.getTokenExpireAt() != null && c.getTokenExpireAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expiré");
        }
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Fichier manquant");
        if (!Objects.requireNonNull(file.getOriginalFilename(), "").toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Seuls les PDF sont acceptés");
        }

        // stockage
        String baseDir = storageDir == null || storageDir.isBlank() ? "uploads" : storageDir;
        Path targetDir = Path.of(baseDir, "candidats", c.getPublicId().toString());
        try {
            Files.createDirectories(targetDir);
            String safeName = System.currentTimeMillis() + "-" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            Path dest = targetDir.resolve(safeName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            String url = "/files/candidats/" + c.getPublicId() + "/" + safeName; // prévoir un handler statique
            c.setCvUrl(url);
            c.setDateReception(LocalDateTime.now());
            c.setStatut(StatutCandidat.CV_RECU);
            Candidat saved = repo.save(c);

            // broadcast événement
            Map<String, Object> evt = new HashMap<>();
            evt.put("type", "cv_recu");
            Map<String, Object> cdto = new HashMap<>();
            cdto.put("publicId", saved.getPublicId());
            cdto.put("email", saved.getEmail());
            cdto.put("nom", saved.getNom());
            cdto.put("cvUrl", saved.getCvUrl());
            evt.put("candidat", cdto);
            ws.broadcast(evt);

            return saved;
        } catch (IOException e) {
            throw new RuntimeException("Erreur stockage fichier", e);
        }
    }

    private String opt(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    private String buildReplyTo(String token) {
        // cv+<token>@domaine
        if (replyToBase.contains("@")) {
            String[] parts = replyToBase.split("@", 2);
            return parts[0] + "+" + token + "@" + parts[1];
        }
        return replyToBase + "+" + token;
    }
}
