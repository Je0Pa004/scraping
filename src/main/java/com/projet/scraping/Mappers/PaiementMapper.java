package com.projet.scraping.Mappers;

import com.projet.scraping.DtoResponse.PaiementResponse;
import com.projet.scraping.entities.Paiement;
import com.projet.scraping.DtoRequest.PaiementCrudRequest;
import com.projet.scraping.security.model.User;
import com.projet.scraping.security.repository.UserRepository;
import com.projet.scraping.repositories.AbonnementRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaiementMapper {

    private final UserRepository utilisateurRepository;
    private final AbonnementRepository abonnementRepository;

    public Paiement toEntity(PaiementCrudRequest req) {
        if (req.getUtilisateurPublicId() == null) {
            throw new IllegalArgumentException("Utilisateur PublicId ne peut pas être null");
        }

        User user = utilisateurRepository.findByPublicId(req.getUtilisateurPublicId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + req.getUtilisateurPublicId()));
        return Paiement.builder()
                .montant(req.getMontant())
                .datePaiement(req.getDatePaiement())
                .methode(req.getMethode())
                .statut(req.getStatut())
                .utilisateur(user)
                .build();
    }

    public void update(Paiement entity, PaiementCrudRequest req) {
        if (req.getMontant() != null) entity.setMontant(req.getMontant());
        if (req.getDatePaiement() != null) entity.setDatePaiement(req.getDatePaiement());
        if (req.getMethode() != null) entity.setMethode(req.getMethode());
        if (req.getStatut() != null) entity.setStatut(req.getStatut());
        if (req.getUtilisateurPublicId() != null) {
            User user = utilisateurRepository.findByPublicId(req.getUtilisateurPublicId())
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + req.getUtilisateurPublicId()));
            entity.setUtilisateur(user);
        }
    }

    public PaiementResponse toResponse(Paiement p) {
        return PaiementResponse.builder()
                .publicId(p.getPublicId())
                .montant(p.getMontant())
                .datePaiement(p.getDatePaiement())
                .methode(p.getMethode())
                .statut(p.getStatut())
                .utilisateurPublicId(p.getUtilisateur().getPublicId())
                .build();
    }
}
