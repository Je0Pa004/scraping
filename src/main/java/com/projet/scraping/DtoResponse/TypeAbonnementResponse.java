package com.projet.scraping.DtoResponse;

import java.util.UUID;

import com.projet.scraping.entities.enums.SubscriptionType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TypeAbonnementResponse {
    private Long id;
    private UUID publicId;
    private String nom;
    private String description;
    private Double cout;
    private Integer duree;
    private Integer nombreScrapingMax;
    private Integer nombreProfilsMax;
    private SubscriptionType type;
    private Boolean estActif;
}
