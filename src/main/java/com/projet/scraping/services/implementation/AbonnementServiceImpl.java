package com.projet.scraping.services.implementation;


import com.projet.scraping.DtoRequest.AbonnementRequest;
import com.projet.scraping.DtoResponse.AbonnementResponse;
import com.projet.scraping.Mappers.AbonnementMapper;
import com.projet.scraping.entities.Abonnement;
import com.projet.scraping.entities.TypeAbonnement;
import com.projet.scraping.entities.enums.SubscriptionType;
import com.projet.scraping.repositories.AbonnementRepository;
import com.projet.scraping.repositories.ScrapingRepository;
import com.projet.scraping.repositories.TypeAbonnementRepository;
import com.projet.scraping.security.repository.UserRepository;
import com.projet.scraping.services.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AbonnementServiceImpl implements AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final AbonnementMapper abonnementMapper;
    private final ScrapingRepository scrapingRepository;
    private final TypeAbonnementRepository typeAbonnementRepository;
    private final UserRepository userRepository;

    // ===== CRUD =====
    @Override
    public AbonnementResponse save(AbonnementRequest req) {
        TypeAbonnement type = typeAbonnementRepository.findByPublicId(UUID.fromString(String.valueOf(req.getTypeAbonnementPublicId())))
                .orElseThrow(() -> new IllegalArgumentException("TypeAbonnement introuvable: " + req.getTypeAbonnementPublicId()));

        com.projet.scraping.security.model.User user = userRepository.findByPublicId(UUID.fromString(String.valueOf(req.getUtilisateurPublicId())))
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + req.getUtilisateurPublicId()));

        Abonnement e = Abonnement.builder()
                .dateDebut(LocalDate.now())
                .dateFin(calculateEndDate(LocalDate.now(), type.getType()))
                .prix(BigDecimal.valueOf(type.getCout()))
                .statut(true)
                .nombreScraping(0)
                .utilisateur(user)
                .typeAbonnement(type)
                .build();

        e = abonnementRepository.save(e);
        return abonnementMapper.toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public AbonnementResponse get(Long id) {
        Abonnement e = abonnementRepository.findById(id).orElse(null);
        return e != null ? abonnementMapper.toResponse(e) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbonnementResponse> getAll() {
        return abonnementRepository.findAll().stream().map(abonnementMapper::toResponse).toList();
    }

    @Override
    public AbonnementResponse update(Long id, AbonnementRequest req) {
        Abonnement e = abonnementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abonnement introuvable: " + id));
        abonnementMapper.update(e, req);
        e = abonnementRepository.save(e);
        return abonnementMapper.toResponse(e);
    }

    @Override
    public void delete(Long id) {
        abonnementRepository.deleteById(id);
    }

    // ===== Autres méthodes =====
    /**
      Calcule la date de fin d'abonnement en fonction de la date de début et du type d'abonnement.
      @param startDate la date de début de l'abonnement
      @param type le type d'abonnement (MENSUEL, TRIMESTRIEL, ANNUEL)
      @return la date de fin calculée
     */
    @Override
    public LocalDate calculateEndDate(LocalDate startDate, SubscriptionType type) {
        return switch (type) {
            case MENSUEL -> startDate.plusMonths(1);
            case TRIMESTRIEL -> startDate.plusMonths(3);
            case ANNUEL -> startDate.plusYears(1);
        };
    }

    /**
      Vérifie si l'utilisateur est éligible au renouvellement de son abonnement.
      L'utilisateur est éligible si son abonnement est expiré ou s'il a atteint le quota maximum de scraping.
      @return true si éligible, false sinon
     */
    @Override
    public boolean checkEligibilityForRenewal(Long userId) {
        Abonnement activeAbonnement = abonnementRepository.findTopByUtilisateur_IdOrderByDateDebutDesc(userId).orElse(null);
        if (activeAbonnement == null || !activeAbonnement.getStatut()) return true; // No active, can renew

        LocalDate today = LocalDate.now();
        if (activeAbonnement.getDateFin().isBefore(today)) return true; // Expired

        // vérifie si le quota utilisé est au max
        long totalScraped = scrapingRepository.sumNombreProfilScrapeByUtilisateurId(userId);
        return totalScraped >= activeAbonnement.getTypeAbonnement().getNombreScrapingMax();
    }

    /**
      Renouvelle l'abonnement de l'utilisateur avec un nouveau type d'abonnement.
      Désactive l'ancien abonnement actif s'il existe et crée un nouveau.
      @return la réponse de l'abonnement renouvelé
      @throws IllegalArgumentException si l'utilisateur n'est pas éligible ou si le type n'existe pas
     */
    @Override
    public AbonnementResponse renewSubscription(Long userId, Long typeAbonnementId) {
        if (!checkEligibilityForRenewal(userId)) {
            throw new IllegalStateException("L'utilisateur n'est pas éligible");
        }

        TypeAbonnement type = typeAbonnementRepository.findById(typeAbonnementId)
                .orElseThrow(() -> new IllegalArgumentException("TypeAbonnement not found"));


        Abonnement old = abonnementRepository.findTopByUtilisateur_IdOrderByDateDebutDesc(userId).orElse(null);
        if (old != null && old.getStatut()) {
            old.setStatut(false);
            abonnementRepository.save(old);
        }

        // Creation d'un nouveau
        Abonnement newAbonnement = new Abonnement();
        newAbonnement.setDateDebut(LocalDate.now());
        newAbonnement.setDateFin(calculateEndDate(LocalDate.now(), type.getType()));
        newAbonnement.setPrix(BigDecimal.valueOf(type.getCout()));
        newAbonnement.setStatut(true);
        newAbonnement.setNombreScraping(0);
        com.projet.scraping.security.model.User user = new com.projet.scraping.security.model.User();
        user.setId(userId);
        newAbonnement.setUtilisateur(user);
        newAbonnement.setTypeAbonnement(type);

        newAbonnement = abonnementRepository.save(newAbonnement);
        return abonnementMapper.toResponse(newAbonnement);
    }

    /**
      Vérifie si l'abonnement de l'utilisateur est actif.
      Un abonnement est actif si la date de fin n'est pas dépassée.
      @return true si l'abonnement est actif, false sinon
     */
    @Override
    public boolean isSubscriptionActive(Long userId) {
        return abonnementRepository.hasActiveSubscription(userId, LocalDate.now());
    }

    /**
      Calcule le quota restant de scrapings pour l'utilisateur.
      Le quota restant est la différence entre le maximum autorisé et le nombre déjà utilisé.
      @return le nombre de scrapings restants, 0 si aucun abonnement actif
     */
    @Override
    public int getRemainingQuota(Long userId) {
        Abonnement a = abonnementRepository.findTopByUtilisateur_IdOrderByDateDebutDesc(userId).orElse(null);
        if (a == null || !a.getStatut()) return 0;
        int max = a.getTypeAbonnement().getNombreScrapingMax();
        long used = scrapingRepository.sumNombreProfilScrapeByUtilisateurId(userId);
        return Math.max(0, max - (int) used);
    }
}
