package com.projet.scraping.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projet.scraping.entities.enums.StatutCandidat;
import com.projet.scraping.security.model.User;
import com.projet.scraping.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Candidat extends BaseEntity {
    @Column(nullable = false)
    private String email;

    private String nom;
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCandidat statut;

    private String cvUrl;

    private LocalDateTime dateInvitation;
    private LocalDateTime dateReception;

    @Column(length = 1000)
    private String notes;

    // Lien éventuel vers un profil scrappé (publicId)
    private String profilPublicId;

    // Recruteur ayant invité
    @ManyToOne
    @JoinColumn(name = "recruteur_id")
    @JsonIgnore
    private User recruteur;

    // Gestion des tokens d'invitation (Reply-To)
    @Column(name = "token_invite", unique = true)
    private String tokenInvite;

    private LocalDateTime tokenExpireAt;
}
