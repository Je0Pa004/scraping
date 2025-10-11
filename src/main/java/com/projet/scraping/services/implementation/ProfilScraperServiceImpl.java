package com.projet.scraping.services.implementation;


import com.projet.scraping.DtoRequest.ProfilScraperRequest;
import com.projet.scraping.DtoResponse.ProfilScraperResponse;
import com.projet.scraping.Mappers.ProfilScraperMapper;
import com.projet.scraping.entities.ProfilScraper;
import com.projet.scraping.repositories.ProfilScraperRepository;
import com.projet.scraping.services.ProfilScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfilScraperServiceImpl implements ProfilScraperService {

    private final ProfilScraperRepository profilScraperRepository;
    private final ProfilScraperMapper profilScraperMapper;

    @Override
    public ProfilScraperResponse save(ProfilScraperRequest req) {
        ProfilScraper e = profilScraperMapper.toEntity(req);
        e = profilScraperRepository.save(e);
        return profilScraperMapper.toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilScraperResponse get(Long id) {
        ProfilScraper e = profilScraperRepository.findById(id).orElse(null);
        return e != null ? profilScraperMapper.toResponse(e) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfilScraperResponse> getAll() {
        return profilScraperRepository.findAll().stream().map(profilScraperMapper::toResponse).toList();
    }

    @Override
    public ProfilScraperResponse update(Long id, ProfilScraperRequest req) {
        ProfilScraper e = profilScraperRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ProfilScraper introuvable: " + id));
        profilScraperMapper.update(e, req);
        e = profilScraperRepository.save(e);
        return profilScraperMapper.toResponse(e);
    }

    @Override
    public void delete(Long id) {
        profilScraperRepository.deleteById(id);
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
        List<ProfilScraper> profiles = profilScraperRepository.findByScraping_Id(scrapingId);
        return profiles.stream().map(profilScraperMapper::toResponse).toList();
    }
}
