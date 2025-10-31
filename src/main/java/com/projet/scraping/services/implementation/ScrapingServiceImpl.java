package com.projet.scraping.services.implementation;


import com.projet.scraping.DtoRequest.ProfilScraperRequest;
import com.projet.scraping.DtoRequest.ScrapingRequest;
import com.projet.scraping.DtoResponse.ScrapingResponse;
import com.projet.scraping.Mappers.ScrapingMapper;
import com.projet.scraping.entities.Scraping;
import com.projet.scraping.entities.enums.ScrapingStatus;
import com.projet.scraping.repositories.ScrapingRepository;
import com.projet.scraping.security.model.User;
import com.projet.scraping.security.repository.UserRepository;
import com.projet.scraping.services.AbonnementService;
import com.projet.scraping.services.ProfilScraperService;
import com.projet.scraping.services.ScrapingService;
import com.projet.scraping.external.PythonScraperClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ScrapingServiceImpl implements ScrapingService {

    private final ScrapingRepository scrapingRepository;
    private final ScrapingMapper scrapingMapper;
    private final AbonnementService abonnementService;
    private final ProfilScraperService profilScraperService;
    private final UserRepository userRepository;
    private final PythonScraperClient pythonScraperClient;

    // Feature flags (configurables via application.properties)
    @Value("${app.scraping.require-subscription:true}")
    private boolean requireSubscription;

    @Value("${app.scraping.enforce-quota:true}")
    private boolean enforceQuota;

    // ===== CRUD =====
    @Override
    public ScrapingResponse save(ScrapingRequest req) {
        // Bind request to current user to satisfy mapper
        req.setUtilisateurPublicId(getCurrentUserId());
        Scraping e = scrapingMapper.toEntity(req);
        // Force ownership to current user
        com.projet.scraping.security.model.User current = getCurrentUser();
        e.setUtilisateur(current);
        e.setDateDemande(LocalDate.now());
        e = scrapingRepository.save(e);
        return scrapingMapper.toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public ScrapingResponse get(Long id) {
        Scraping e = scrapingRepository.findByIdAndUtilisateur_PublicId(id, getCurrentUserId()).orElse(null);
        return e != null ? scrapingMapper.toResponse(e) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScrapingResponse> getAll() {
        return scrapingRepository.findByUtilisateur_PublicId(getCurrentUserId())
                .stream().map(scrapingMapper::toResponse).toList();
    }

    @Override
    public ScrapingResponse update(Long id, ScrapingRequest req) {
        Scraping e = scrapingRepository.findByIdAndUtilisateur_PublicId(id, getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Scraping introuvable: " + id));
        scrapingMapper.update(e, req);
        // Ensure ownership not changed
        e.setUtilisateur(getCurrentUser());
        e = scrapingRepository.save(e);
        return scrapingMapper.toResponse(e);
    }

    @Override
    public void delete(Long id) {
        scrapingRepository.findByIdAndUtilisateur_PublicId(id, getCurrentUserId())
                .ifPresent(s -> scrapingRepository.deleteById(s.getId()));
    }

    @Override
    public void deleteByPublicId(UUID publicId) {
        Scraping e = scrapingRepository.findByPublicIdAndUtilisateur_PublicId(publicId, getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Scraping introuvable: " + publicId));
        scrapingRepository.deleteById(e.getId());
    }

    // ===== Autres méthodes =====
    /**
       Vérifie si l'abonnement de l'utilisateur est actif.
       @return true si l'abonnement est actif, false sinon
     */
    @Override
    public boolean isSubscriptionActive(Long userId) {
        return abonnementService.isSubscriptionActive(userId);
    }

    /**
      Calcule le quota restant de scrapings pour l'utilisateur.
      @return le nombre de scrapings restants
     */
    @Override
    public int getRemainingQuota(Long userId) {
        return abonnementService.getRemainingQuota(userId);
    }

    /**
      Vérifie si l'utilisateur est éligible au scraping.
      L'utilisateur est éligible si son abonnement est actif et qu'il a un quota.
      @return true si l'utilisateur éligible, false sinon
     */
    @Override
    public boolean isEligibleForScraping(Long userId) {
        return isSubscriptionActive(userId) && getRemainingQuota(userId) > 0;
    }

    /**
      Effectuer un scraping tout en vérifiant l'éligibilité de l'utilisateur.
      sauvegarde les profils, et met à jour le statut du scraping.
      @param req la requête de scraping
      @return la réponse du scraping effectué
      @throws IllegalStateException vérifie si l'utilisateur n'est pas éligible
     */
    @Override
    public ScrapingResponse performScraping(ScrapingRequest req) {
        // Always bind to current user
        User user = getCurrentUser();
        
        // Vérifier si l'abonnement est actif
        if (requireSubscription && !isSubscriptionActive(user.getId())) {
            throw new IllegalStateException("Votre abonnement n'est pas actif. Veuillez activer votre compte en effectuant un paiement.");
        }
        
        // Vérifier le quota restant
        int remainingQuota = getRemainingQuota(user.getId());
        if (enforceQuota && remainingQuota <= 0) {
            throw new IllegalStateException("Vous n'avez plus de requêtes disponibles. Veuillez renouveler votre abonnement.");
        }

        // Bind request to current user to satisfy mapper
        req.setUtilisateurPublicId(user.getPublicId());
        Scraping scraping = scrapingMapper.toEntity(req);
        scraping.setUtilisateur(user);
        scraping.setDateDemande(LocalDate.now());
        scraping.setStatut(ScrapingStatus.EN_COURS);
        scraping.setNombreProfilScrape(0);
        scraping = scrapingRepository.save(scraping);

        // Appel au service Python FastAPI
        Integer limit = req.getMaxResults() != null ? req.getMaxResults() : 50;
        List<Map<String, Object>> raw = pythonScraperClient.scrape(req, limit);

        // Mapping vers ProfilScraperRequest
        final UUID scrapingPublicId = scraping.getPublicId();
        List<ProfilScraperRequest> profiles = (raw == null ? List.<Map<String, Object>>of() : raw).stream()
                .map(m -> ProfilScraperRequest.builder()
                        .nom(String.valueOf(Objects.toString(m.getOrDefault("nom", "N/A"))))
                        .description(String.valueOf(Objects.toString(m.getOrDefault("description", "N/A"))))
                        .email(String.valueOf(Objects.toString(m.getOrDefault("email", "N/A"))))
                        .telephone(String.valueOf(Objects.toString(m.getOrDefault("telephone", "N/A"))))
                        .urlSource(String.valueOf(Objects.toString(
                                m.getOrDefault("profile_url", m.getOrDefault("url", m.getOrDefault("site_web", "")))
                        )))
                        .statut("VALIDE")
                        .scrapingPublicId(scrapingPublicId)
                        .build())
                .collect(Collectors.toList());

        // Sauvegarde des profils
        if (!profiles.isEmpty()) {
            profilScraperService.saveProfiles(profiles, scraping.getId());
        }
        scraping.setNombreProfilScrape(profiles.size());
        scraping.setStatut(ScrapingStatus.TERMINE);
        scraping = scrapingRepository.save(scraping);

        return scrapingMapper.toResponse(scraping);
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
        // Fallback: try to resolve via username/email
        String email = auth.getName();
        return userRepository.findByEmail(email).map(User::getPublicId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
    }

    private User getCurrentUser() {
        UUID pid = getCurrentUserId();
        return userRepository.findByPublicId(pid)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable: " + pid));
    }
}
