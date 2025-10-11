package com.projet.scraping.Mappers;

import com.projet.scraping.DtoRequest.ScrapingRequest;
import com.projet.scraping.DtoResponse.ScrapingResponse;
import com.projet.scraping.entities.Scraping;
import com.projet.scraping.security.model.User;
import com.projet.scraping.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScrapingMapper {

    private final UserRepository utilisateurRepository;

    public Scraping toEntity(ScrapingRequest req) {
        User user = utilisateurRepository.findByPublicId(req.getUtilisateurPublicId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + req.getUtilisateurPublicId()));
        return Scraping.builder()
                .utilisateur(user)
                .source(req.getSource())
                .titre(req.getTitre())
                .secteur(req.getSecteur())
                .localisation(req.getLocalisation())
                .entreprise(req.getEntreprise())
                .emploi(req.getEmploi())
                .tailleEntreprise(req.getTailleEntreprise())
                .build();
    }

    public void update(Scraping e, ScrapingRequest req) {
        e.setSource(req.getSource());
        e.setTitre(req.getTitre());
        e.setSecteur(req.getSecteur());
        e.setLocalisation(req.getLocalisation());
        e.setEntreprise(req.getEntreprise());
        e.setEmploi(req.getEmploi());
        e.setTailleEntreprise(req.getTailleEntreprise());
        if (req.getUtilisateurPublicId() != null) {
            User user = utilisateurRepository.findByPublicId(req.getUtilisateurPublicId())
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + req.getUtilisateurPublicId()));
            e.setUtilisateur(user);
        }
    }

    public ScrapingResponse toResponse(Scraping s) {
        return ScrapingResponse.builder()
                .publicId(s.getPublicId())
                .dateDemande(s.getDateDemande())
                .source(s.getSource())
                .statut(s.getStatut())
                .titre(s.getTitre())
                .secteur(s.getSecteur())
                .localisation(s.getLocalisation())
                .entreprise(s.getEntreprise())
                .emploi(s.getEmploi())
                .tailleEntreprise(s.getTailleEntreprise())
                .nombreProfilScrape(s.getNombreProfilScrape())
                .utilisateurPublicId(s.getUtilisateur().getPublicId())
                .build();
    }
}

