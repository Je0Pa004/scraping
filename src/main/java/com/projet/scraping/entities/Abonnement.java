package com.projet.scraping.entities;

import com.projet.scraping.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Abonnement extends BaseEntity {

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "prix", nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(name = "statut", nullable = false)
    private Boolean statut; // ex: true pour ACTIF, false pour INACTIF

    @Column(name = "nombre_scraping", nullable = false)
    private Integer nombreScraping;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private com.projet.scraping.security.model.User utilisateur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "type_abonnement_id")
    private TypeAbonnement typeAbonnement;
}
