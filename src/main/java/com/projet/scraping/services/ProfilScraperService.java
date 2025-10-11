package com.projet.scraping.services;

import com.projet.scraping.DtoRequest.ProfilScraperRequest;
import com.projet.scraping.DtoResponse.ProfilScraperResponse;

import java.util.List;

public interface ProfilScraperService {
    // CRUD
    ProfilScraperResponse save(ProfilScraperRequest req);
    ProfilScraperResponse get(Long id);
    List<ProfilScraperResponse> getAll();
    ProfilScraperResponse update(Long id, ProfilScraperRequest req);
    void delete(Long id);

    // Autres méthodes
    void saveProfiles(List<ProfilScraperRequest> reqs, Long scrapingId);
    List<ProfilScraperResponse> getProfilesByScraping(Long scrapingId);
}
