package com.projet.scraping.repositories;

import com.projet.scraping.entities.ProfilScraper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProfilScraperRepository extends JpaRepository<ProfilScraper, Long> {

    List<ProfilScraper> findByScraping_PublicId(UUID scrapingId);

    List<ProfilScraper> findByScraping_Id(Long scrapingId);

    @Query("select count(p) from ProfilScraper p where p.scraping.publicId = :scrapingId")
    long countByScraping(@Param("scrapingId") UUID scrapingId);
}
