package com.projet.scraping.DtoResponse;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AbonnementResponse {
    private UUID publicId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean statut;
    private Integer nombreScraping;
    private BigDecimal prix;
    private UUID utilisateurPublicId;
    private TypeAbonnementResponse typeAbonnement;
}
