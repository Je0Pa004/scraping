package com.projet.scraping.services.implementation;


import com.projet.scraping.DtoRequest.ProfilScraperRequest;
import com.projet.scraping.DtoResponse.ProfilScraperResponse;
import com.projet.scraping.Mappers.ProfilScraperMapper;
import com.projet.scraping.entities.ProfilScraper;
import com.projet.scraping.repositories.ProfilScraperRepository;
import com.projet.scraping.services.ProfilScraperService;
import com.projet.scraping.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfilScraperServiceImpl implements ProfilScraperService {

    private final ProfilScraperRepository profilScraperRepository;
    private final ProfilScraperMapper profilScraperMapper;
    private final com.projet.scraping.repositories.ScrapingRepository scrapingRepository;
    private final UserRepository userRepository;

    @Override
    public ProfilScraperResponse save(ProfilScraperRequest req) {
        // Verify the target scraping belongs to current user
        java.util.UUID current = getCurrentUserId();
        scrapingRepository.findByPublicIdAndUtilisateur_PublicId(req.getScrapingPublicId(), current)
                .orElseThrow(() -> new IllegalArgumentException("Scraping introuvable ou non autorisé: " + req.getScrapingPublicId()));

        ProfilScraper e = profilScraperMapper.toEntity(req);
        e = profilScraperRepository.save(e);
        return profilScraperMapper.toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilScraperResponse get(Long id) {
        ProfilScraper e = profilScraperRepository.findByIdAndScraping_Utilisateur_PublicId(id, getCurrentUserId()).orElse(null);
        return e != null ? profilScraperMapper.toResponse(e) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfilScraperResponse> getAll() {
        return profilScraperRepository.findByScraping_Utilisateur_PublicId(getCurrentUserId())
                .stream().map(profilScraperMapper::toResponse).toList();
    }

    @Override
    public ProfilScraperResponse update(Long id, ProfilScraperRequest req) {
        ProfilScraper e = profilScraperRepository.findByIdAndScraping_Utilisateur_PublicId(id, getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("ProfilScraper introuvable: " + id));
        // If changing scraping, ensure new scraping belongs to current user
        if (req.getScrapingPublicId() != null) {
            scrapingRepository.findByPublicIdAndUtilisateur_PublicId(req.getScrapingPublicId(), getCurrentUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Scraping introuvable ou non autorisé: " + req.getScrapingPublicId()));
        }
        profilScraperMapper.update(e, req);
        e = profilScraperRepository.save(e);
        return profilScraperMapper.toResponse(e);
    }

    @Override
    public void delete(Long id) {
        profilScraperRepository.findByIdAndScraping_Utilisateur_PublicId(id, getCurrentUserId())
                .ifPresent(p -> profilScraperRepository.deleteById(p.getId()));
    }

    // ===== Business methods =====
    /**
      Sauvegarde une liste de profils scrapés associés à un scraping.
      @param reqs : la liste des requêtes de profils
     */
    @Override
    public void saveProfiles(List<ProfilScraperRequest> reqs, Long scrapingId) {
        for (ProfilScraperRequest req : reqs) {
            ProfilScraper e = profilScraperMapper.toEntity(req);
            com.projet.scraping.entities.Scraping s = new com.projet.scraping.entities.Scraping();
            s.setId(scrapingId);
            e.setScraping(s);
            profilScraperRepository.save(e);
        }
    }

    /**
      Récupère la liste des profils scrapés associés à un scraping.
      @return la liste des réponses de profils
     */
    @Override
    public List<ProfilScraperResponse> getProfilesByScraping(Long scrapingId) {
        // Ownership via parent scraping id cannot be checked with publicId here; ensure scraping belongs to user
        com.projet.scraping.entities.Scraping s = scrapingRepository.findByIdAndUtilisateur_PublicId(scrapingId, getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Scraping introuvable: " + scrapingId));
        List<ProfilScraper> profiles = profilScraperRepository.findByScraping_Id(s.getId());
        return profiles.stream().map(profilScraperMapper::toResponse).toList();
    }

    private java.util.UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof com.projet.scraping.security.UserDetailsImpl u) {
            return u.getId();
        }
        // Fallback
        String email = auth.getName();
        return userRepository.findByEmail(email).map(com.projet.scraping.security.model.User::getPublicId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
    }
}
