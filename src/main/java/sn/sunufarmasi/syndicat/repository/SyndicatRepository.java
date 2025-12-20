package sn.sunufarmasi.syndicat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.syndicat.entity.Syndicat;
import sn.sunufarmasi.syndicat.enums.StatutSyndicat;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Syndicat
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface SyndicatRepository extends JpaRepository<Syndicat, UUID> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR IDENTIFIANTS UNIQUES
    // ═══════════════════════════════════════════════════════════

    Optional<Syndicat> findByCode(String code);

    Optional<Syndicat> findByUsername(String username);

    boolean existsByCode(String code);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByTelephone(String telephone);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR ZONE GÉOGRAPHIQUE
    // ═══════════════════════════════════════════════════════════

    Optional<Syndicat> findByCommuneId(UUID communeId);

    Optional<Syndicat> findByDepartementId(UUID departementId);

    List<Syndicat> findByRegionId(UUID regionId);

    boolean existsByCommuneId(UUID communeId);

    boolean existsByDepartementId(UUID departementId);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR TYPE ET STATUT
    // ═══════════════════════════════════════════════════════════

    List<Syndicat> findByType(TypeSyndicat type);

    List<Syndicat> findByStatut(StatutSyndicat statut);

    List<Syndicat> findByTypeAndStatut(TypeSyndicat type, StatutSyndicat statut);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR RESPONSABLE
    // ═══════════════════════════════════════════════════════════

    List<Syndicat> findByResponsableId(UUID responsableId);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR NOM
    // ═══════════════════════════════════════════════════════════

    List<Syndicat> findByNomContainingIgnoreCase(String nom);

    // ═══════════════════════════════════════════════════════════
    // REQUÊTES PERSONNALISÉES
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouver le syndicat qui gère une commune donnée
     * (soit directement la commune, soit le département de la commune)
     */
    @Query("""
        SELECT s FROM Syndicat s
        WHERE s.statut = sn.sunufarmasi.syndicat.enums.StatutSyndicat.ACTIF
        AND (
            (s.type = sn.sunufarmasi.syndicat.enums.TypeSyndicat.COMMUNE AND s.commune.id = :communeId)
            OR
            (s.type = sn.sunufarmasi.syndicat.enums.TypeSyndicat.DEPARTEMENT AND s.departement.id = (
                SELECT c.departement.id FROM Commune c WHERE c.id = :communeId
            ))
        )
        ORDER BY s.type ASC
        """)
    List<Syndicat> findSyndicatsGestionnairesByCommune(@Param("communeId") UUID communeId);

    /**
     * Trouver tous les syndicats actifs d'une région
     */
    @Query("SELECT s FROM Syndicat s WHERE s.region.id = :regionId AND s.statut = sn.sunufarmasi.syndicat.enums.StatutSyndicat.ACTIF")
    List<Syndicat> findActifsByRegion(@Param("regionId") UUID regionId);

    /**
     * Compter les pharmacies gérées par un syndicat
     * (via la table pharmacies)
     */
    @Query("SELECT COUNT(p) FROM Pharmacie p WHERE p.syndicat.id = :syndicatId")
    Long countPharmaciesBySyndicat(@Param("syndicatId") UUID syndicatId);

    /**
     * Vérifier si un syndicat existe déjà pour cette zone
     */
    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Syndicat s
        WHERE (s.commune.id = :communeId OR s.departement.id = :departementId)
        AND s.statut != sn.sunufarmasi.syndicat.enums.StatutSyndicat.INACTIF
        """)
    boolean existsByZone(@Param("communeId") UUID communeId, @Param("departementId") UUID departementId);

    /**
     * Liste des syndicats avec abonnement expiré
     */
    @Query("""
        SELECT s FROM Syndicat s
        WHERE s.statut = sn.sunufarmasi.syndicat.enums.StatutSyndicat.ACTIF
        AND s.essaiGratuit = false
        AND s.dateFinAbonnement < :today
        """)
    List<Syndicat> findWithExpiredSubscriptionBefore(@Param("today") LocalDate today);

    /**
     * Liste des syndicats avec abonnement expiré (méthode helper)
     */
    default List<Syndicat> findWithExpiredSubscription() {
        return findWithExpiredSubscriptionBefore(LocalDate.now());
    }

    /**
     * Liste des syndicats en période d'essai qui expire bientôt
     */
    @Query("""
        SELECT s FROM Syndicat s
        WHERE s.statut = sn.sunufarmasi.syndicat.enums.StatutSyndicat.ACTIF
        AND s.essaiGratuit = true
        AND s.dateFinEssai BETWEEN :dateDebut AND :dateFin
        """)
    List<Syndicat> findWithTrialExpiringSoonBetween(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );

    /**
     * Liste des syndicats en période d'essai qui expire dans les 7 prochains jours
     */
    default List<Syndicat> findWithTrialExpiringSoon() {
        return findWithTrialExpiringSoonBetween(LocalDate.now(), LocalDate.now().plusDays(7));
    }
}