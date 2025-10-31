package com.projet.scraping.services;

import com.projet.scraping.DtoRequest.PaiementCrudRequest;
import com.projet.scraping.DtoResponse.PaiementResponse;
import com.projet.scraping.entities.enums.PaymentStatus;

import java.util.List;

public interface PaiementService {
    // CRUD
    PaiementResponse save(PaiementCrudRequest req);
    PaiementResponse get(Long id);
    List<PaiementResponse> getAll();
    PaiementResponse update(Long id, PaiementCrudRequest req);
    void delete(Long id);

    // Autres méthodes
    PaiementResponse processPayment(PaiementCrudRequest req, Long typeAbonnementId);
    PaiementResponse updatePaymentStatus(Long paiementId, PaymentStatus status, Long typeAbonnementId);

    // Stripe Payment Intent
    String createPaymentIntent(Long amount, String currency, Long typeAbonnementId);
}
