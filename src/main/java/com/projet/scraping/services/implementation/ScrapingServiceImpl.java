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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScrapingServiceImpl implements ScrapingService {

    private final ScrapingRepository scrapingRepository;
    private final ScrapingMapper scrapingMapper;
    private final AbonnementService abonnementService;
    private final ProfilScraperService profilScraperService;
    private final UserRepository userRepository;

    // ===== CRUD =====
    @Override
    public ScrapingResponse save(ScrapingRequest req) {
        Scraping e = scrapingMapper.toEntity(req);
        e.setDateDemande(LocalDate.now());
        e = scrapingRepository.save(e);
        return scrapingMapper.toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public ScrapingResponse get(Long id) {
        Scraping e = scrapingRepository.findById(id).orElse(null);
        return e != null ? scrapingMapper.toResponse(e) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScrapingResponse> getAll() {
        return scrapingRepository.findAll().stream().map(scrapingMapper::toResponse).toList();
    }

    @Override
    public ScrapingResponse update(Long id, ScrapingRequest req) {
        Scraping e = scrapingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scraping introuvable: " + id));
        scrapingMapper.update(e, req);
        e = scrapingRepository.save(e);
        return scrapingMapper.toResponse(e);
    }

    @Override
    public void delete(Long id) {
        scrapingRepository.deleteById(id);
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
        User user = userRepository.findByPublicId(req.getUtilisateurPublicId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + req.getUtilisateurPublicId()));
        if (!isEligibleForScraping(user.getId())) {
            throw new IllegalStateException("Pas éligible pour le scraping");
        }

        Scraping scraping = scrapingMapper.toEntity(req);
        scraping.setDateDemande(LocalDate.now());
        scraping.setStatut(ScrapingStatus.EN_COURS);
        scraping.setNombreProfilScrape(0);
        scraping = scrapingRepository.save(scraping);


        List<ProfilScraperRequest> profiles = List.of(

        );
        profilScraperService.saveProfiles(profiles, scraping.getId());
        scraping.setNombreProfilScrape(profiles.size());
        scraping.setStatut(ScrapingStatus.TERMINE);
        scraping = scrapingRepository.save(scraping);

        return scrapingMapper.toResponse(scraping);
    }
}
