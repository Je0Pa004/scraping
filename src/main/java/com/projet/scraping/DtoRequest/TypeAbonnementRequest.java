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

    @NotNull @Positive
    private Double cout;

    @NotNull @Positive
    private Integer nombreScrapingMax;

    @NotNull
    private SubscriptionType type;
}
