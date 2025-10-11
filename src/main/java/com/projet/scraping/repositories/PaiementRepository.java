package com.projet.scraping.repositories;

import com.projet.scraping.entities.Paiement;
import com.projet.scraping.entities.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findByUtilisateur_Id(Long utilisateurId);

    @Query("select p from Paiement p where p.utilisateur.id = :utilisateurId and p.statut = :status order by p.datePaiement desc")
    List<Paiement> findByUtilisateurAndStatusOrderByDateDesc(@Param("utilisateurId") Long utilisateurId,
                                                             @Param("status") PaymentStatus status);

    default Optional<Paiement> findLatestSuccessful(Long utilisateurId) {
        List<Paiement> list = findByUtilisateurAndStatusOrderByDateDesc(utilisateurId, PaymentStatus.SUCCESS);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
    }
}
