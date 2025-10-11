package com.projet.scraping.services.implementation;


import com.projet.scraping.DtoRequest.PaiementCrudRequest;
import com.projet.scraping.DtoResponse.PaiementResponse;
import com.projet.scraping.Mappers.PaiementMapper;
import com.projet.scraping.entities.Paiement;
import com.projet.scraping.entities.enums.PaymentStatus;
import com.projet.scraping.repositories.PaiementRepository;
import com.projet.scraping.repositories.TypeAbonnementRepository;
import com.projet.scraping.security.repository.UserRepository;
import com.projet.scraping.services.AbonnementService;
import com.projet.scraping.services.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // ===== CRUD =====
    @Override
    public PaiementResponse save(PaiementCrudRequest req) {
        com.projet.scraping.security.model.User user = userRepository.findByPublicId(UUID.fromString(String.valueOf(req.getUtilisateurPublicId())))
                .orElseThrow(() -> new IllegalArgumentException("User not found with publicId: " + req.getUtilisateurPublicId()));

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
        Paiement e = paiementRepository.findById(id).orElse(null);
        return e != null ? paiementMapper.toResponse(e) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaiementResponse> getAll() {
        return paiementRepository.findAll().stream().map(paiementMapper::toResponse).toList();
    }

    @Override
    public PaiementResponse update(Long id, PaiementCrudRequest req) {
        Paiement e = paiementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable: " + id));
        paiementMapper.update(e, req);
        e = paiementRepository.save(e);
        return paiementMapper.toResponse(e);
    }

    @Override
    public void delete(Long id) {
        paiementRepository.deleteById(id);
    }

    // ===== Antres méthodes =====
    /**
      Traite un paiement en créant une entrée de paiement en attente.
      Le montant est défini en fonction du type d'abonnement sélectionné.
      @param req: la requête de paiement
      @return la réponse du paiement traité
      @throws IllegalArgumentException si le type d'abonnement n'existe pas
     */
    @Override
    public PaiementResponse processPayment(PaiementCrudRequest req, Long typeAbonnementId) {
        var type = typeAbonnementRepository.findById(typeAbonnementId)
                .orElseThrow(() -> new IllegalArgumentException("TypeAbonnement not found"));

        Paiement paiement = paiementMapper.toEntity(req);
        paiement.setMontant(type.getCout());
        paiement.setDatePaiement(LocalDate.now());
        paiement.setStatut(PaymentStatus.PENDING);
        paiement = paiementRepository.save(paiement);
        return paiementMapper.toResponse(paiement);
    }

    /**
     * Met à jour le statut d'un paiement.
     * Si le statut est SUCCESS, active l'abonnement associé en le renouvelant.
     * @param paiementId l'identifiant du paiement
     * @param status le nouveau statut du paiement
     * @param typeAbonnementId l'identifiant du type d'abonnement pour le renouvellement
     * @return la réponse du paiement mis à jour
     * @throws IllegalArgumentException si le paiement n'existe pas
     */
    @Override
    public PaiementResponse updatePaymentStatus(Long paiementId, PaymentStatus status, Long typeAbonnementId) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new IllegalArgumentException("Paiement not found"));
        paiement.setStatut(status);
        paiement = paiementRepository.save(paiement);

        if (status == PaymentStatus.SUCCESS) {
            abonnementService.renewSubscription(paiement.getUtilisateur().getId(), typeAbonnementId);
        }

        return paiementMapper.toResponse(paiement);
    }
}
