package com.projet.scraping.repositories;

import com.projet.scraping.entities.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CandidatRepository extends JpaRepository<Candidat, Long> {
    Optional<Candidat> findByPublicId(UUID publicId);
    Optional<Candidat> findByTokenInvite(String tokenInvite);
}
