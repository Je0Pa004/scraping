package com.projet.scraping.entities;

import com.projet.scraping.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.projet.scraping.security.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projet.scraping.entities.enums.ApiSource;
import com.projet.scraping.entities.enums.ScrapingStatus;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Scraping extends BaseEntity {

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    private LocalDate dateDemande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScrapingStatus statut; // ex: EN_COURS, TERMINE, ECHEC

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String secteur;

    @Column(nullable = false)
    private String localisation;

    @Column(nullable = false)
    private String entreprise;

    @Column(nullable = false)
    private String emploi;

    @Column(nullable = false)
    private String tailleEntreprise;

    @Column(nullable = false)
    private Integer nombreProfilScrape;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id")
    @JsonIgnore
    private User utilisateur;

    @OneToMany(mappedBy = "scraping", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfilScraper> profils;
}
