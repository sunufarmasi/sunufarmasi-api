package sn.sunufarmasi.garde.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.garde.entity.PlanningGarde;
import sn.sunufarmasi.garde.entity.StatutPlanning;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les plannings de garde
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface PlanningGardeRepository extends JpaRepository<PlanningGarde, UUID> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR SYNDICAT
    // ═══════════════════════════════════════════════════════════

    List<PlanningGarde> findBySyndicatIdOrderByDateDebutDesc(UUID syndicatId);

    Page<PlanningGarde> findBySyndicatId(UUID syndicatId, Pageable pageable);

    List<PlanningGarde> findBySyndicatIdAndStatut(UUID syndicatId, StatutPlanning statut);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PÉRIODE
    // ═══════════════════════════════════════════════════════════

    /**
     * Planning actif pour une date donnée
     */
    @Query("SELECT p FROM PlanningGarde p WHERE p.syndicat.id = :syndicatId " +
           "AND p.statut = 'PUBLIE' AND :date BETWEEN p.dateDebut AND p.dateFin")
    Optional<PlanningGarde> findPlanningActif(
            @Param("syndicatId") UUID syndicatId,
            @Param("date") LocalDate date
    );

    /**
     * Plannings publiés pour une période
     */
    @Query("SELECT p FROM PlanningGarde p WHERE p.statut = 'PUBLIE' " +
           "AND ((p.dateDebut BETWEEN :debut AND :fin) OR (p.dateFin BETWEEN :debut AND :fin) " +
           "OR (p.dateDebut <= :debut AND p.dateFin >= :fin))")
    List<PlanningGarde> findPlanningsPubliesParPeriode(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );

    /**
     * Planning actuel d'un syndicat (publié et couvrant aujourd'hui)
     */
    @Query("SELECT p FROM PlanningGarde p WHERE p.syndicat.id = :syndicatId " +
           "AND p.statut = 'PUBLIE' AND :today BETWEEN p.dateDebut AND p.dateFin")
    Optional<PlanningGarde> findPlanningActuel(
            @Param("syndicatId") UUID syndicatId,
            @Param("today") LocalDate today
    );

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR STATUT
    // ═══════════════════════════════════════════════════════════

    List<PlanningGarde> findByStatutOrderByDateDebutDesc(StatutPlanning statut);

    /**
     * Plannings prêts pour publication automatique
     */
    @Query("SELECT p FROM PlanningGarde p WHERE p.statut = 'VALIDE' " +
           "AND p.publicationAuto = true " +
           "AND p.datePublicationPrevue <= :now")
    List<PlanningGarde> findPlanningsAPublierAuto(@Param("now") LocalDateTime now);

    /**
     * Plannings à archiver (terminés depuis plus de X jours)
     */
    @Query("SELECT p FROM PlanningGarde p WHERE p.statut = 'PUBLIE' " +
           "AND p.dateFin < :date")
    List<PlanningGarde> findPlanningsAArchiver(@Param("date") LocalDate date);

    // ═══════════════════════════════════════════════════════════
    // VÉRIFICATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifier si un planning existe déjà pour cette période
     */
    @Query("SELECT COUNT(p) > 0 FROM PlanningGarde p WHERE p.syndicat.id = :syndicatId " +
           "AND p.id != :excludeId " +
           "AND ((p.dateDebut BETWEEN :debut AND :fin) OR (p.dateFin BETWEEN :debut AND :fin))")
    boolean existsChevauchement(
            @Param("syndicatId") UUID syndicatId,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            @Param("excludeId") UUID excludeId
    );

    /**
     * Compter les plannings par statut pour un syndicat
     */
    @Query("SELECT p.statut, COUNT(p) FROM PlanningGarde p " +
           "WHERE p.syndicat.id = :syndicatId GROUP BY p.statut")
    List<Object[]> countByStatutForSyndicat(@Param("syndicatId") UUID syndicatId);

    /**
     * Dernier planning publié d'un syndicat
     */
    @Query("SELECT p FROM PlanningGarde p WHERE p.syndicat.id = :syndicatId " +
           "AND p.statut = 'PUBLIE' ORDER BY p.datePublication DESC LIMIT 1")
    Optional<PlanningGarde> findDernierPlanningPublie(@Param("syndicatId") UUID syndicatId);

    /**
     * Recherche par syndicat + période exacte (utile pour éviter les doublons au seed)
     */
    Optional<PlanningGarde> findBySyndicatIdAndDateDebutAndDateFin(
            UUID syndicatId, LocalDate dateDebut, LocalDate dateFin);

    // ═══════════════════════════════════════════════════════════
    // SUPER ADMIN - TOUS LES PLANNINGS AVEC FILTRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Tous les plannings (tous syndicats) avec filtres optionnels : mois, syndicat, statut
     */
    @Query("SELECT p FROM PlanningGarde p WHERE " +
           "((p.dateDebut BETWEEN :debut AND :fin) OR (p.dateFin BETWEEN :debut AND :fin) " +
           "OR (p.dateDebut <= :debut AND p.dateFin >= :fin)) " +
           "AND (:syndicatId IS NULL OR p.syndicat.id = :syndicatId) " +
           "AND (:statut IS NULL OR p.statut = :statut) " +
           "ORDER BY p.dateDebut DESC")
    List<PlanningGarde> findAllAdminWithFilters(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            @Param("syndicatId") UUID syndicatId,
            @Param("statut") StatutPlanning statut);
}
