package com.projet.scraping.DtoRequest;

import com.projet.scraping.entities.enums.ApiSource;
import lombok.*;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ScrapingRequest {
    private UUID utilisateurPublicId;
    private ApiSource source;           // LINKEDIN, PAGE_JAUNE, GOOGLE_MAP
    private String titre;
    private String secteur;
    private String localisation;
    private String entreprise;
    private String emploi;
    private String tailleEntreprise;
}
