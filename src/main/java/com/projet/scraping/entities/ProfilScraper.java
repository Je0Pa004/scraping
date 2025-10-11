package com.projet.scraping.entities;

import com.projet.scraping.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfilScraper extends BaseEntity {

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String telephone;
    @Column(nullable = false)
    private String urlSource;

    @Column(nullable = false)
    private String statut; // ex: VALIDE, INVALIDE

    @ManyToOne(optional = false)
    @JoinColumn(name = "scraping_id")
    private Scraping scraping;
}
