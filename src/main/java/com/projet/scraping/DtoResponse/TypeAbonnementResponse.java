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
    private UUID publicId;
    private String nom;
    private Double cout;
    private Integer nombreScrapingMax;
    private SubscriptionType type;
}
