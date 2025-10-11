package com.projet.scraping.services;

import com.projet.scraping.DtoRequest.AbonnementRequest;
import com.projet.scraping.DtoResponse.AbonnementResponse;
import com.projet.scraping.entities.enums.SubscriptionType;

import java.time.LocalDate;
import java.util.List;


public interface AbonnementService {
    // CRUD
    AbonnementResponse save(AbonnementRequest req);
    AbonnementResponse get(Long id);
    List<AbonnementResponse> getAll();
    AbonnementResponse update(Long id, AbonnementRequest req);
    void delete(Long id);

    // Autres méthodes
    LocalDate calculateEndDate(LocalDate startDate, SubscriptionType type);
    boolean checkEligibilityForRenewal(Long userId);
    AbonnementResponse renewSubscription(Long userId, Long typeAbonnementId);
    boolean isSubscriptionActive(Long userId);
    int getRemainingQuota(Long userId);
}
