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
                .description(req.getDescription())
                .cout(req.getCout())
                .duree(req.getDuree())
                .nombreScrapingMax(req.getNombreScrapingMax())
                .nombreProfilsMax(req.getNombreProfilsMax())
                .type(req.getType())
                .build();
    }

    public void update(TypeAbonnement entity, TypeAbonnementRequest req) {
        entity.setNom(req.getNom());
        entity.setDescription(req.getDescription());
        entity.setCout(req.getCout());
        entity.setDuree(req.getDuree());
        entity.setNombreScrapingMax(req.getNombreScrapingMax());
        entity.setNombreProfilsMax(req.getNombreProfilsMax());
        entity.setType(req.getType());
    }

    public TypeAbonnementResponse toResponse(TypeAbonnement e) {
        return TypeAbonnementResponse.builder()
                .id(e.getId())
                .publicId(e.getPublicId())
                .nom(e.getNom())
                .description(e.getDescription())
                .cout(e.getCout())
                .duree(e.getDuree())
                .nombreScrapingMax(e.getNombreScrapingMax())
                .nombreProfilsMax(e.getNombreProfilsMax())
                .type(e.getType())
                .estActif(e.getEstActif())
                .build();
    }
}

