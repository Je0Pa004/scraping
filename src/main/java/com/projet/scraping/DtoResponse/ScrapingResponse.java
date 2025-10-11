package com.projet.scraping.DtoResponse;

import com.projet.scraping.entities.enums.ApiSource;
import com.projet.scraping.entities.enums.ScrapingStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ScrapingResponse {
    private UUID publicId;
    private LocalDate dateDemande;
    private ApiSource source;
    private ScrapingStatus statut;
    private String titre;
    private String secteur;
    private String localisation;
    private String entreprise;
    private String emploi;
    private String tailleEntreprise;
    private Integer nombreProfilScrape;
    private UUID utilisateurPublicId;
}
