package com.projet.scraping.DtoResponse;

import com.projet.scraping.entities.enums.PaymentMethod;
import com.projet.scraping.entities.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaiementResponse {
    private UUID publicId;
    private Double montant;
    private LocalDate datePaiement;
    private PaymentMethod methode;
    private PaymentStatus statut;
    private UUID utilisateurPublicId;
}
