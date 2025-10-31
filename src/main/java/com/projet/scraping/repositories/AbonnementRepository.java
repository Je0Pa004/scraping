package com.projet.scraping.repositories;

import com.projet.scraping.entities.Abonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    List<Abonnement> findByUtilisateur_Id(Long utilisateurId);

    Optional<Abonnement> findTopByUtilisateur_IdOrderByDateDebutDesc(Long utilisateurId);

    @Query("select (count(a) > 0) from Abonnement a where a.utilisateur.id = :utilisateurId and a.statut = true and a.dateFin >= :today")
    boolean hasActiveSubscription(@Param("utilisateurId") Long utilisateurId, @Param("today") LocalDate today);

    Optional<Abonnement> findByUtilisateurPublicIdAndStatut(UUID utilisateurPublicId, boolean statut);

    @Query("select a from Abonnement a where a.utilisateur.publicId = :utilisateurPublicId and a.statut = true and a.dateFin >= :today order by a.dateDebut desc")
    List<Abonnement> findActiveSubscriptions(@Param("utilisateurPublicId") UUID utilisateurPublicId, @Param("today") LocalDate today);

    // Per-user helpers
    List<Abonnement> findByUtilisateur_PublicId(UUID utilisateurPublicId);

    Optional<Abonnement> findByIdAndUtilisateur_PublicId(Long id, UUID utilisateurPublicId);
}
