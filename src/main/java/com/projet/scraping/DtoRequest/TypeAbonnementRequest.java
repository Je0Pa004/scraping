package com.projet.scraping.DtoRequest;

import com.projet.scraping.entities.enums.SubscriptionType;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TypeAbonnementRequest {
    @NotBlank
    private String nom;

    private String description;

    @NotNull @Positive
    private Double cout;

    @NotNull @Positive
    private Integer duree;

    @NotNull @Positive
    private Integer nombreScrapingMax;

    @NotNull @Positive
    private Integer nombreProfilsMax;

    @NotNull
    private SubscriptionType type;
}
