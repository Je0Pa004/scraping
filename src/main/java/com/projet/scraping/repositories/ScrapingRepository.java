package com.projet.scraping.repositories;

import com.projet.scraping.entities.Scraping;
import com.projet.scraping.entities.enums.ApiSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ScrapingRepository extends JpaRepository<Scraping, Long> {

    Optional<Scraping> findByPublicId(UUID publicId);

    @Query("select coalesce(sum(s.nombreProfilScrape),0) from Scraping s where s.utilisateur.id = :uid and s.dateDemande between :start and :end")
    long sumProfilesInPeriod(@Param("uid") Long utilisateurId,
                             @Param("start") LocalDate start,
                             @Param("end") LocalDate end);

    @Query("select coalesce(sum(s.nombreProfilScrape),0) from Scraping s where s.utilisateur.id = :userId")
    long sumNombreProfilScrapeByUtilisateurId(@Param("userId") Long userId);
}
