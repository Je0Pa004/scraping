package com.projet.scraping.services.implementation;


import com.projet.scraping.DtoRequest.PaiementCrudRequest;
import com.projet.scraping.DtoResponse.PaiementResponse;
import com.projet.scraping.Mappers.PaiementMapper;
import com.projet.scraping.entities.Abonnement;
import com.projet.scraping.entities.Paiement;
import com.projet.scraping.entities.TypeAbonnement;
import com.projet.scraping.entities.enums.PaymentStatus;
import com.projet.scraping.repositories.AbonnementRepository;
import com.projet.scraping.repositories.PaiementRepository;
import com.projet.scraping.repositories.TypeAbonnementRepository;
import com.projet.scraping.security.repository.UserRepository;
import com.projet.scraping.services.AbonnementService;
import com.projet.scraping.services.PaiementService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository paiementRepository;
    private final PaiementMapper paiementMapper;
    private final AbonnementService abonnementService;
    private final TypeAbonnementRepository typeAbonnementRepository;
    private final UserRepository userRepository;
    private final AbonnementRepository abonnementRepository;

    @Value("${stripe.secret.key:sk_test_51QEXAMPLE...}") // Replace with your actual secret key
    private String stripeSecretKey;

    // ===== CRUD =====
    @Override
    public PaiementResponse save(PaiementCrudRequest req) {
        // Bind to current user and ignore client-provided user
        com.projet.scraping.security.model.User user = getCurrentUser();
        req.setUtilisateurPublicId(user.getPublicId());
        Paiement e = paiementMapper.toEntity(req);
        e.setUtilisateur(user);
        if (e.getDatePaiement() == null) {
            e.setDatePaiement(LocalDate.now());
        }
        if (e.getStatut() == null) {
            e.setStatut(PaymentStatus.PENDING);
        }
        e = paiementRepository.save(e);
        return paiementMapper.toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public PaiementResponse get(Long id) {
        Paiement e = paiementRepository.findByIdAndUtilisateur_PublicId(id, getCurrentUserId()).orElse(null);
        return e != null ? paiementMapper.toResponse(e) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaiementResponse> getAll() {
        return paiementRepository.findByUtilisateur_PublicId(getCurrentUserId())
                .stream().map(paiementMapper::toResponse).toList();
    }

    @Override
    public PaiementResponse update(Long id, PaiementCrudRequest req) {
        Paiement e = paiementRepository.findByIdAndUtilisateur_PublicId(id, getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable: " + id));
        // ensure ownership remains current user
        req.setUtilisateurPublicId(getCurrentUserId());
        paiementMapper.update(e, req);
        e.setUtilisateur(getCurrentUser());
        e = paiementRepository.save(e);
        return paiementMapper.toResponse(e);
    }

    @Override
    public void delete(Long id) {
        paiementRepository.findByIdAndUtilisateur_PublicId(id, getCurrentUserId())
                .ifPresent(p -> paiementRepository.deleteById(p.getId()));
    }

    // ===== Autres méthodes =====
    /**
      Traite un paiement en créant une entrée de paiement et un abonnement.
      Le montant est défini en fonction du type d'abonnement sélectionné.
      L'abonnement est créé mais reste inactif jusqu'à ce que le paiement soit validé.
      @param req: la requête de paiement
      @param typeAbonnementId: l'ID du type d'abonnement
      @return la réponse du paiement traité
      @throws IllegalArgumentException si le type d'abonnement ou l'utilisateur n'existe pas
     */
    @Override
    public PaiementResponse processPayment(PaiementCrudRequest req, Long typeAbonnementId) {
        TypeAbonnement type = typeAbonnementRepository.findById(typeAbonnementId)
                .orElseThrow(() -> new IllegalArgumentException("TypeAbonnement not found"));
        // Always use current user
        com.projet.scraping.security.model.User user = getCurrentUser();
        req.setUtilisateurPublicId(user.getPublicId());

        // Créer l'abonnement (INACTIF tant que le paiement n'est pas validé)
        Abonnement abonnement = Abonnement.builder()
                .dateDebut(LocalDate.now())
                .dateFin(abonnementService.calculateEndDate(LocalDate.now(), type.getType()))
                .prix(java.math.BigDecimal.valueOf(type.getCout()))
                .statut(false) // INACTIF par défaut
                .quotaUtilise(0)
                .utilisateur(user)
                .typeAbonnement(type)
                .build();
        abonnement = abonnementRepository.save(abonnement);

        // Créer le paiement lié à l'abonnement
        Paiement paiement = paiementMapper.toEntity(req);
        paiement.setMontant(type.getCout());
        paiement.setDatePaiement(LocalDate.now());
        paiement.setStatut(PaymentStatus.PENDING);
        paiement.setUtilisateur(user);
        paiement.setAbonnement(abonnement);
        paiement = paiementRepository.save(paiement);
        
        return paiementMapper.toResponse(paiement);
    }

    /**
     * Met à jour le statut d'un paiement.
     * Si le statut est SUCCESS, active l'abonnement associé.
     * @param paiementId l'identifiant du paiement
     * @param status le nouveau statut du paiement
     * @param typeAbonnementId l'identifiant du type d'abonnement (optionnel)
     * @return la réponse du paiement mis à jour
     * @throws IllegalArgumentException si le paiement n'existe pas
     */
    @Override
    public PaiementResponse updatePaymentStatus(Long paiementId, PaymentStatus status, Long typeAbonnementId) {
        Paiement paiement = paiementRepository.findByIdAndUtilisateur_PublicId(paiementId, getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Paiement not found"));
        paiement.setStatut(status);
        paiement = paiementRepository.save(paiement);

        // Si le paiement est réussi, activer l'abonnement
        if (status == PaymentStatus.SUCCESS && paiement.getAbonnement() != null) {
            Abonnement abonnement = paiement.getAbonnement();
            abonnement.setStatut(true); // Activer l'abonnement
            abonnementRepository.save(abonnement);
        }

        return paiementMapper.toResponse(paiement);
    }

    /**
     * Creates a Stripe PaymentIntent for the given amount and currency.
     * @param amount the amount in cents
     * @param currency the currency code (e.g., "eur")
     * @param typeAbonnementId the subscription type ID
     * @return the client secret of the PaymentIntent
     * @throws RuntimeException if Stripe API call fails
     */
    @Override
    public String createPaymentIntent(Long amount, String currency, Long typeAbonnementId) {
        Stripe.apiKey = stripeSecretKey;

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency)
                    .putMetadata("typeAbonnementId", typeAbonnementId.toString())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentIntent.getClientSecret();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create PaymentIntent: " + e.getMessage(), e);
        }
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof com.projet.scraping.security.UserDetailsImpl u) {
            return u.getId();
        }
        String email = auth.getName();
        return userRepository.findByEmail(email).map(com.projet.scraping.security.model.User::getPublicId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
    }

    private com.projet.scraping.security.model.User getCurrentUser() {
        UUID pid = getCurrentUserId();
        return userRepository.findByPublicId(pid).orElseThrow(() -> new IllegalStateException("Utilisateur introuvable: " + pid));
    }
}
