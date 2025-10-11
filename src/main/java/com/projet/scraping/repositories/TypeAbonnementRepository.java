package com.projet.scraping.repositories;

import com.projet.scraping.entities.TypeAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TypeAbonnementRepository extends JpaRepository<TypeAbonnement, Long> {
    Optional<TypeAbonnement> findByPublicId(UUID publicId);
}
