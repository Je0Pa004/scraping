package com.projet.scraping.entities;

import com.projet.scraping.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;
import com.projet.scraping.entities.enums.PaymentMethod;
import com.projet.scraping.entities.enums.PaymentStatus;
import com.projet.scraping.security.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Paiement extends BaseEntity {

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false)
    private Double montant;

    @Column(nullable = false)
    private LocalDate datePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod methode; // MOBILE_MONEY, CARTE, VIREMENT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus statut; // SUCCESS, PENDING, FAILED

    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id")
    @JsonIgnore
    private User utilisateur;
}
