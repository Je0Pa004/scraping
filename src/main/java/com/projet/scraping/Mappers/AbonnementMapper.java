package com.projet.scraping.Mappers;

import com.projet.scraping.DtoRequest.AbonnementRequest;
import com.projet.scraping.DtoResponse.AbonnementResponse;
import com.projet.scraping.DtoResponse.TypeAbonnementResponse;
import com.projet.scraping.entities.Abonnement;
import com.projet.scraping.entities.TypeAbonnement;
import com.projet.scraping.repositories.TypeAbonnementRepository;
import com.projet.scraping.security.model.User;
import com.projet.scraping.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AbonnementMapper {

    private final TypeAbonnementRepository typeAbonnementRepository;
    private final UserRepository utilisateurRepository;
    private final TypeAbonnementMapper typeAbonnementMapper;

    public Abonnement toEntity(AbonnementRequest req) {
        TypeAbonnement type = typeAbonnementRepository.findByPublicId(UUID.fromString(String.valueOf(req.getTypeAbonnementPublicId())))
                .orElseThrow(() -> new IllegalArgumentException("TypeAbonnement introuvable: " + req.getTypeAbonnementPublicId()));
        User user = utilisateurRepository.findByPublicId(UUID.fromString(String.valueOf(req.getUtilisateurPublicId())))
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + req.getUtilisateurPublicId()));
        return Abonnement.builder()
                .dateDebut(req.getDateDebut())
                .dateFin(req.getDateFin())
                .statut(req.getStatut())
                .quotaTotal(type.getNombreScrapingMax())
                .quotaUtilise(req.getQuotaUtilise() != null ? req.getQuotaUtilise() : 0)
                .prix(BigDecimal.valueOf(type.getCout()))
                .typeAbonnement(type)
                .utilisateur(user)
                .build();
    }

    public void update(Abonnement entity, AbonnementRequest req) {
        if (req.getDateDebut() != null) entity.setDateDebut(req.getDateDebut());
        if (req.getDateFin() != null) entity.setDateFin(req.getDateFin());
        if (req.getStatut() != null) entity.setStatut(req.getStatut());
        if (req.getQuotaUtilise() != null) entity.setQuotaUtilise(req.getQuotaUtilise());
        if (req.getTypeAbonnementPublicId() != null) {
            var type = typeAbonnementRepository.findByPublicId(UUID.fromString(String.valueOf(req.getTypeAbonnementPublicId())))
                    .orElseThrow(() -> new IllegalArgumentException("TypeAbonnement introuvable: " + req.getTypeAbonnementPublicId()));
            entity.setTypeAbonnement(type);
            entity.setPrix(BigDecimal.valueOf(type.getCout()));
            entity.setQuotaTotal(type.getNombreScrapingMax());
        }
    }

    public AbonnementResponse toResponse(Abonnement e) {
        TypeAbonnementResponse type = typeAbonnementMapper.toResponse(e.getTypeAbonnement());
        return AbonnementResponse.builder()
                .publicId(e.getPublicId())
                .dateDebut(e.getDateDebut())
                .dateFin(e.getDateFin())
                .statut(e.getStatut())
                .quotaUtilise(e.getQuotaUtilise())
                .prix(e.getPrix())
                .utilisateurPublicId(e.getUtilisateur().getPublicId())
                .typeAbonnement(type)
                .build();
    }
}

