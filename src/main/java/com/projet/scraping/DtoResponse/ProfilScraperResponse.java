package com.projet.scraping.DtoResponse;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProfilScraperResponse {
    private UUID publicId;
    private String nom;
    private String description;
    private String email;
    private String telephone;
    private String urlSource;
    private String statut;
    private UUID scrapingPublicId;
}
