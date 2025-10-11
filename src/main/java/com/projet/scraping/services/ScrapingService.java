package com.projet.scraping.services;

import com.projet.scraping.DtoRequest.ScrapingRequest;
import com.projet.scraping.DtoResponse.ScrapingResponse;

import java.util.List;

public interface ScrapingService {
    // CRUD
    ScrapingResponse save(ScrapingRequest req);
    ScrapingResponse get(Long id);
    List<ScrapingResponse> getAll();
    ScrapingResponse update(Long id, ScrapingRequest req);
    void delete(Long id);

    // Autres méthodes
    boolean isSubscriptionActive(Long userId);
    int getRemainingQuota(Long userId);
    boolean isEligibleForScraping(Long userId);
    ScrapingResponse performScraping(ScrapingRequest req);
}
