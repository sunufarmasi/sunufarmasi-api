package sn.sunufarmasi.garde.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.garde.entity.Garde;
import sn.sunufarmasi.garde.enums.StatutGarde;
import sn.sunufarmasi.garde.enums.TypeGarde;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Garde
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface GardeRepository extends JpaRepository<Garde, UUID> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR SEMAINE ET ZONE (PRINCIPAL)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouver les gardes pour une semaine donnée dans une COMMUNE
     * Inclut les gardes stockées au niveau commune ET au niveau département parent de cette commune
     */
    @Query("SELECT g FROM Garde g " +
            "LEFT JOIN g.commune c " +
            "LEFT JOIN g.departement dept " +
            "WHERE (c.id = :communeId " +
            "   OR dept.id = (SELECT com.departement.id FROM Commune com WHERE com.id = :communeId)) " +
            "AND g.dateDebut <= :dateFin AND g.dateFin >= :dateDebut " +
            "AND g.statut NOT IN ('ANNULEE') " +
            "ORDER BY g.dateDebut")
    List<Garde> findBySemaineAndCommune(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("communeId") UUID communeId
    );

    /**
     * Trouver les gardes pour une semaine donnée dans un DEPARTEMENT
     * Inclut les gardes stockées au niveau département ET au niveau commune de ce département
     */
    @Query("SELECT g FROM Garde g " +
            "LEFT JOIN g.departement dept " +
            "LEFT JOIN g.commune c " +
            "LEFT JOIN c.departement cd " +
            "WHERE (dept.id = :departementId OR cd.id = :departementId) " +
            "AND g.dateDebut <= :dateFin AND g.dateFin >= :dateDebut " +
            "AND g.statut NOT IN ('ANNULEE') " +
            "ORDER BY g.dateDebut")
    List<Garde> findBySemaineAndDepartement(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("departementId") UUID departementId
    );

    /**
     * Trouver la garde pour une date spécifique dans une commune
     * Inclut les gardes stockées au niveau commune ET au niveau département parent de cette commune
     */
    @Query("SELECT g FROM Garde g " +
            "LEFT JOIN g.commune c " +
            "LEFT JOIN g.departement dept " +
            "WHERE (c.id = :communeId " +
            "   OR dept.id = (SELECT com.departement.id FROM Commune com WHERE com.id = :communeId)) " +
            "AND :date BETWEEN g.dateDebut AND g.dateFin " +
            "AND g.statut NOT IN ('ANNULEE')")
    List<Garde> findByDateAndCommune(
            @Param("date") LocalDate date,
            @Param("communeId") UUID communeId
    );

    /**
     * Trouver la garde pour une date spécifique dans un département
     * Inclut les gardes stockées au niveau département ET au niveau commune de ce département
     */
    @Query("SELECT g FROM Garde g " +
            "LEFT JOIN g.departement dept " +
            "LEFT JOIN g.commune c " +
            "LEFT JOIN c.departement cd " +
            "WHERE (dept.id = :departementId OR cd.id = :departementId) " +
            "AND :date BETWEEN g.dateDebut AND g.dateFin " +
            "AND g.statut NOT IN ('ANNULEE')")
    List<Garde> findByDateAndDepartement(
            @Param("date") LocalDate date,
            @Param("departementId") UUID departementId
    );

    /**
     * Trouver les gardes de la semaine en cours (toutes zones)
     */
    @Query("SELECT g FROM Garde g WHERE :today BETWEEN g.dateDebut AND g.dateFin " +
            "AND g.statut NOT IN ('ANNULEE') " +
            "ORDER BY g.zoneNom")
    List<Garde> findGardesSemaineEnCours(@Param("today") LocalDate today);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PHARMACIE
    // ═══════════════════════════════════════════════════════════

    /**
     * Gardes d'une pharmacie
     */
    List<Garde> findByPharmacieId(UUID pharmacieId);

    /**
     * Supprimer toutes les gardes d'une pharmacie (avant suppression de la pharmacie)
     */
    void deleteByPharmacieId(UUID pharmacieId);

    /**
     * Gardes à venir d'une pharmacie
     */
    @Query("SELECT g FROM Garde g WHERE g.pharmacie.id = :pharmacieId " +
            "AND g.dateDebut >= :today AND g.statut NOT IN ('ANNULEE', 'TERMINEE') " +
            "ORDER BY g.dateDebut")
    List<Garde> findGardesAVenirByPharmacie(
            @Param("pharmacieId") UUID pharmacieId,
            @Param("today") LocalDate today
    );

    /**
     * Historique des gardes d'une pharmacie
     */
    @Query("SELECT g FROM Garde g WHERE g.pharmacie.id = :pharmacieId " +
            "ORDER BY g.dateDebut DESC")
    Page<Garde> findHistoriqueByPharmacie(
            @Param("pharmacieId") UUID pharmacieId,
            Pageable pageable
    );

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PLANNING
    // ═══════════════════════════════════════════════════════════

    List<Garde> findByPlanningId(UUID planningId);

    @Query("SELECT g FROM Garde g WHERE g.planning.id = :planningId ORDER BY g.dateDebut, g.zoneNom")
    List<Garde> findByPlanningIdOrderByDateDebutAndZone(@Param("planningId") UUID planningId);

    // ═══════════════════════════════════════════════════════════
    // VÉRIFICATIONS D'EXISTENCE (pour éviter doublons)
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifier si une garde existe déjà pour cette semaine/commune/type
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Garde g " +
            "WHERE g.dateDebut = :dateDebut AND g.dateFin = :dateFin " +
            "AND g.commune.id = :communeId AND g.typeGarde = :typeGarde")
    boolean existsBySemaineAndCommuneAndType(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("communeId") UUID communeId,
            @Param("typeGarde") TypeGarde typeGarde
    );

    /**
     * Vérifier si une garde existe déjà pour cette semaine/département/type
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Garde g " +
            "WHERE g.dateDebut = :dateDebut AND g.dateFin = :dateFin " +
            "AND g.departement.id = :departementId AND g.typeGarde = :typeGarde")
    boolean existsBySemaineAndDepartementAndType(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("departementId") UUID departementId,
            @Param("typeGarde") TypeGarde typeGarde
    );

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR STATUT
    // ═══════════════════════════════════════════════════════════

    List<Garde> findByStatut(StatutGarde statut);

    @Query("SELECT g FROM Garde g WHERE g.planning.id = :planningId AND g.statut = :statut")
    List<Garde> findByPlanningIdAndStatut(
            @Param("planningId") UUID planningId,
            @Param("statut") StatutGarde statut
    );

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Nombre de gardes par pharmacie sur une période
     */
    @Query("SELECT g.pharmacie.id, COUNT(g) FROM Garde g " +
            "WHERE g.dateDebut >= :debut AND g.dateFin <= :fin " +
            "GROUP BY g.pharmacie.id")
    List<Object[]> countGardesParPharmacie(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );

    /**
     * Prochaines gardes non confirmées
     */
    @Query("SELECT g FROM Garde g WHERE g.dateDebut > :today " +
            "AND g.confirmeParPharmacie = false AND g.statut = 'PLANIFIEE' " +
            "ORDER BY g.dateDebut")
    List<Garde> findGardesNonConfirmees(@Param("today") LocalDate today);


    // ═══════════════════════════════════════════════════════════════════════════════
// AJOUTER CES MÉTHODES À TON GardeRepository EXISTANT
// (Ne pas remplacer le fichier, juste copier ces méthodes dedans)
// ═══════════════════════════════════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR RÉGION (NOUVEAU)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouver les gardes pour une date spécifique dans une RÉGION (toutes les localités)
     */
//    @Query("SELECT g FROM Garde g WHERE " +
//            "(g.departement.region.id = :regionId OR g.commune.departement.region.id = :regionId) " +
//            "AND :date BETWEEN g.dateDebut AND g.dateFin " +
//            "AND g.statut NOT IN ('ANNULEE')")
//    List<Garde> findByDateAndRegion(
//            @Param("date") LocalDate date,
//            @Param("regionId") UUID regionId
//    );

    @Query("SELECT g FROM Garde g " +
            "LEFT JOIN g.departement dept " +
            "LEFT JOIN dept.region r1 " +
            "LEFT JOIN g.commune c " +
            "LEFT JOIN c.departement cd " +
            "LEFT JOIN cd.region r2 " +
            "WHERE (r1.id = :regionId OR r2.id = :regionId) " +
            "AND :date BETWEEN g.dateDebut AND g.dateFin " +
            "AND g.statut NOT IN ('ANNULEE')")
    List<Garde> findByDateAndRegion(
            @Param("date") LocalDate date,
            @Param("regionId") UUID regionId
    );
    /**
     * Gardes du jour pour une liste de pharmacies (utilisé pour le secteur multi-syndicat)
     */
    @Query("SELECT g FROM Garde g " +
            "WHERE g.pharmacie.id IN :pharmacieIds " +
            "AND :date BETWEEN g.dateDebut AND g.dateFin " +
            "AND g.statut NOT IN ('ANNULEE')")
    List<Garde> findByDateAndPharmacieIds(
            @Param("date") LocalDate date,
            @Param("pharmacieIds") List<UUID> pharmacieIds
    );

    /**
     * Trouver les gardes pour une semaine donnée dans une RÉGION
     * Utilise LEFT JOIN pour couvrir les gardes stockées au niveau département ET commune
     */
    @Query("SELECT g FROM Garde g " +
            "LEFT JOIN g.departement dept " +
            "LEFT JOIN dept.region r1 " +
            "LEFT JOIN g.commune c " +
            "LEFT JOIN c.departement cd " +
            "LEFT JOIN cd.region r2 " +
            "WHERE (r1.id = :regionId OR r2.id = :regionId) " +
            "AND g.dateDebut <= :dateFin AND g.dateFin >= :dateDebut " +
            "AND g.statut NOT IN ('ANNULEE') " +
            "ORDER BY g.dateDebut")
    List<Garde> findBySemaineAndRegion(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("regionId") UUID regionId
    );
}