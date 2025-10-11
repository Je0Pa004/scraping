package com.projet.scraping.DtoRequest;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AbonnementRequest {
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal prix;
    private Boolean statut;

    @NotNull @Positive
    private Integer nombreScraping;

    @NotNull
    private UUID typeAbonnementPublicId;

    @NotNull
    private UUID utilisateurPublicId;
}
