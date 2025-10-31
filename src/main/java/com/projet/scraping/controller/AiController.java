package com.projet.scraping.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @PostMapping("/generate-message")
    public ResponseEntity<Map<String, String>> generateMessage(@RequestBody Map<String, Object> payload) {
        String name = safeString(payload.get("name"));
        String skills = safeString(payload.get("skills"));
        String firstName = name.trim().isEmpty() ? "Candidat" : name.trim().split(" ")[0];
        String mainSkill = skills.contains(",") ? skills.split(",")[0].trim() : (skills.isEmpty() ? "votre domaine" : skills.trim());
        String text = "Bonjour " + firstName + ",\n\n" +
                "J'ai découvert votre profil et vos compétences en " + mainSkill + ". " +
                "Nous serions ravis d'échanger sur une opportunité correspondant à votre parcours.\n\n" +
                "Seriez-vous disponible pour un court échange cette semaine ?\n\n" +
                "Bien cordialement,";
        return ResponseEntity.ok(Collections.singletonMap("text", text));
    }

    private String safeString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
