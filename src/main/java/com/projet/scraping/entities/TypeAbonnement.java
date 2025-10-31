package com.projet.scraping.entities;

import com.projet.scraping.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import com.projet.scraping.entities.enums.SubscriptionType;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypeAbonnement extends BaseEntity {

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double cout;

    @Column(nullable = false)
    private Integer duree; // durée en jours

    @Column(name = "nombre_scraping_max", nullable = false)
    private Integer nombreScrapingMax;

    @Column(name = "nombre_profils_max", nullable = false)
    private Integer nombreProfilsMax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType type; // MENSUEL, TRIMESTRIEL, ANNUEL

    @Column(name = "est_actif", nullable = false)
    @Builder.Default
    private Boolean estActif = true;
}
