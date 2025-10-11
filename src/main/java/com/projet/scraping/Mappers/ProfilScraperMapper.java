package com.projet.scraping.Mappers;

import com.projet.scraping.DtoRequest.ProfilScraperRequest;
import com.projet.scraping.DtoResponse.ProfilScraperResponse;
import com.projet.scraping.entities.ProfilScraper;
import com.projet.scraping.entities.Scraping;
import com.projet.scraping.repositories.ScrapingRepository;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfilScraperMapper {

    private final ScrapingRepository scrapingRepository;

    public ProfilScraper toEntity(ProfilScraperRequest req) {
        Scraping s = scrapingRepository.findByPublicId(req.getScrapingPublicId())
                .orElseThrow(() -> new IllegalArgumentException("Scraping introuvable: " + req.getScrapingPublicId()));
        return ProfilScraper.builder()
                .nom(req.getNom())
                .description(req.getDescription())
                .email(req.getEmail())
                .telephone(req.getTelephone())
                .urlSource(req.getUrlSource())
                .statut(req.getStatut())
                .scraping(s)
                .build();
    }

    public void update(ProfilScraper e, ProfilScraperRequest req) {
        e.setNom(req.getNom());
        e.setDescription(req.getDescription());
        e.setEmail(req.getEmail());
        e.setTelephone(req.getTelephone());
        e.setUrlSource(req.getUrlSource());
        e.setStatut(req.getStatut());
        if (req.getScrapingPublicId() != null) {
            Scraping s = scrapingRepository.findByPublicId(req.getScrapingPublicId())
                    .orElseThrow(() -> new IllegalArgumentException("Scraping introuvable: " + req.getScrapingPublicId()));
            e.setScraping(s);
        }
    }

    public ProfilScraperResponse toResponse(ProfilScraper p) {
        return ProfilScraperResponse.builder()
                .publicId(p.getPublicId())
                .nom(p.getNom())
                .description(p.getDescription())
                .email(p.getEmail())
                .telephone(p.getTelephone())
                .urlSource(p.getUrlSource())
                .statut(p.getStatut())
                .scrapingPublicId(p.getScraping().getPublicId())
                .build();
    }
}

