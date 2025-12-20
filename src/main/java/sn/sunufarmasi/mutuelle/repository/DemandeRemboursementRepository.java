package sn.sunufarmasi.mutuelle.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.mutuelle.entity.DemandeRemboursement;
import sn.sunufarmasi.mutuelle.enums.StatutDemande;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DemandeRemboursementRepository extends JpaRepository<DemandeRemboursement, UUID> {

    Optional<DemandeRemboursement> findByNumero(String numero);

    // Par pharmacie
    Page<DemandeRemboursement> findByPharmacieIdOrderByDateDemandeDesc(UUID pharmacieId, Pageable pageable);

    List<DemandeRemboursement> findByPharmacieIdAndStatut(UUID pharmacieId, StatutDemande statut);

    // Par mutuelle
    List<DemandeRemboursement> findByMutuelleId(UUID mutuelleId);

    Page<DemandeRemboursement> findByPharmacieIdAndMutuelleIdOrderByDateDemandeDesc(
            UUID pharmacieId, UUID mutuelleId, Pageable pageable);

    // Par adhérent
    List<DemandeRemboursement> findByAdherentId(UUID adherentId);

    // Par vente
    Optional<DemandeRemboursement> findByVenteId(UUID venteId);

    // En attente
    @Query("SELECT d FROM DemandeRemboursement d WHERE d.pharmacie.id = :pharmacieId " +
            "AND d.statut IN ('SOUMISE', 'EN_TRAITEMENT', 'ACCEPTEE', 'PARTIELLEMENT_ACCEPTEE') " +
            "ORDER BY d.dateDemande")
    List<DemandeRemboursement> findEnAttente(@Param("pharmacieId") UUID pharmacieId);

    // Par période
    @Query("SELECT d FROM DemandeRemboursement d WHERE d.pharmacie.id = :pharmacieId " +
            "AND d.dateDemande BETWEEN :debut AND :fin ORDER BY d.dateDemande DESC")
    List<DemandeRemboursement> findByPeriode(@Param("pharmacieId") UUID pharmacieId,
                                              @Param("debut") LocalDateTime debut,
                                              @Param("fin") LocalDateTime fin);

    // Statistiques
    @Query("SELECT COALESCE(SUM(d.montantDemande), 0) FROM DemandeRemboursement d " +
            "WHERE d.pharmacie.id = :pharmacieId AND d.dateDemande BETWEEN :debut AND :fin")
    BigDecimal sumMontantDemande(@Param("pharmacieId") UUID pharmacieId,
                                  @Param("debut") LocalDateTime debut,
                                  @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(d.montantPaye), 0) FROM DemandeRemboursement d " +
            "WHERE d.pharmacie.id = :pharmacieId AND d.statut = 'PAYEE' " +
            "AND d.dateDemande BETWEEN :debut AND :fin")
    BigDecimal sumMontantPaye(@Param("pharmacieId") UUID pharmacieId,
                              @Param("debut") LocalDateTime debut,
                              @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(d.montantAccepte), 0) FROM DemandeRemboursement d " +
            "WHERE d.pharmacie.id = :pharmacieId " +
            "AND d.statut IN ('ACCEPTEE', 'PARTIELLEMENT_ACCEPTEE', 'PAYEE') " +
            "AND d.montantPaye IS NULL OR d.montantPaye < d.montantAccepte")
    BigDecimal sumCreances(@Param("pharmacieId") UUID pharmacieId);

    @Query("SELECT d.mutuelle.id, d.mutuelle.nom, COUNT(d), SUM(d.montantDemande), SUM(d.montantPaye) " +
            "FROM DemandeRemboursement d WHERE d.pharmacie.id = :pharmacieId " +
            "AND d.dateDemande BETWEEN :debut AND :fin " +
            "GROUP BY d.mutuelle.id, d.mutuelle.nom")
    List<Object[]> statsByMutuelle(@Param("pharmacieId") UUID pharmacieId,
                                    @Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin);

    @Query("SELECT d.statut, COUNT(d), SUM(d.montantDemande) FROM DemandeRemboursement d " +
            "WHERE d.pharmacie.id = :pharmacieId GROUP BY d.statut")
    List<Object[]> countByStatut(@Param("pharmacieId") UUID pharmacieId);

    // Numérotation
    @Query("SELECT COUNT(d) FROM DemandeRemboursement d WHERE d.pharmacie.id = :pharmacieId " +
            "AND CAST(d.dateDemande AS date) = CAST(:date AS date)")
    Long countDemandesDuJour(@Param("pharmacieId") UUID pharmacieId, @Param("date") LocalDateTime date);
}
