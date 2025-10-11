package com.projet.scraping.DtoRequest;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProfilScraperRequest {
    private String nom;
    private String description;
    private String email;
    private String telephone;
    private String urlSource;
    private String statut;
    private UUID scrapingPublicId;
}
