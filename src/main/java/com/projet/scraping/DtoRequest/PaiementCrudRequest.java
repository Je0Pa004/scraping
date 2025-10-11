package com.projet.scraping.DtoRequest;

import com.projet.scraping.entities.enums.PaymentMethod;
import com.projet.scraping.entities.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaiementCrudRequest {
    @NotNull(message = "L'ID public de l'utilisateur est obligatoire")
    private UUID utilisateurPublicId;
    private Double montant;
    private LocalDate datePaiement;
    private PaymentMethod methode;
    private PaymentStatus statut;
}
