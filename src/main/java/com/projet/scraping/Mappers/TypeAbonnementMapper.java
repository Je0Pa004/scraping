package com.projet.scraping.Mappers;

import com.projet.scraping.DtoRequest.TypeAbonnementRequest;
import com.projet.scraping.DtoResponse.TypeAbonnementResponse;
import com.projet.scraping.entities.TypeAbonnement;
import org.springframework.stereotype.Component;

@Component
public class TypeAbonnementMapper {
    public TypeAbonnement toEntity(TypeAbonnementRequest req) {
        return TypeAbonnement.builder()
                .nom(req.getNom())
                .cout(req.getCout())
                .nombreScrapingMax(req.getNombreScrapingMax())
                .type(req.getType())
                .build();
    }

    public void update(TypeAbonnement entity, TypeAbonnementRequest req) {
        entity.setNom(req.getNom());
        entity.setCout(req.getCout());
        entity.setNombreScrapingMax(req.getNombreScrapingMax());
        entity.setType(req.getType());
    }

    public TypeAbonnementResponse toResponse(TypeAbonnement e) {
        return TypeAbonnementResponse.builder()
                .publicId(e.getPublicId())
                .nom(e.getNom())
                .cout(e.getCout())
                .nombreScrapingMax(e.getNombreScrapingMax())
                .type(e.getType())
                .build();
    }
}

