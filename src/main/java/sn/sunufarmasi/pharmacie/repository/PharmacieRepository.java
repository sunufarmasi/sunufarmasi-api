package sn.sunufarmasi.pharmacie.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les pharmacies
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface PharmacieRepository extends JpaRepository<Pharmacie, UUID> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR IDENTIFIANTS
    // ═══════════════════════════════════════════════════════════

    Optional<Pharmacie> findByCode(String code);

//    Optional<Pharmacie> findByTitulaireId(UUID titulaireId);

    boolean existsByCode(String code);

//    boolean existsByNinea(String ninea);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR LOCALISATION
    // ═══════════════════════════════════════════════════════════

    List<Pharmacie> findByCommuneIdAndStatut(UUID communeId, StatutPharmacie statut);

    @Query("SELECT p FROM Pharmacie p WHERE p.commune.departement.id = :departementId AND p.statut = 'ACTIVE'")
    List<Pharmacie> findByDepartement(@Param("departementId") UUID departementId);

    @Query("SELECT p FROM Pharmacie p WHERE p.commune.departement.region.id = :regionId AND p.statut = 'ACTIVE'")
    List<Pharmacie> findByRegion(@Param("regionId") UUID regionId);

    @Query("SELECT p FROM Pharmacie p WHERE p.syndicat.id = :syndicatId AND p.statut = 'ACTIVE' ORDER BY p.nom")
    List<Pharmacie> findBySyndicat(@Param("syndicatId") UUID syndicatId);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE GÉOGRAPHIQUE (proximité)
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT p FROM Pharmacie p WHERE p.statut = 'ACTIVE' " +
           "AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL " +
           "AND (6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:lon)) + sin(radians(:lat)) * " +
           "sin(radians(p.latitude)))) <= :rayonKm " +
           "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:lon)) + sin(radians(:lat)) * " +
           "sin(radians(p.latitude))))")
    List<Pharmacie> findProximite(@Param("lat") Double lat, @Param("lon") Double lon, @Param("rayonKm") Double rayonKm);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE TEXTUELLE
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT p FROM Pharmacie p WHERE p.statut = 'ACTIVE' AND " +
           "(LOWER(p.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.adresseComplete) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.quartier) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Pharmacie> search(@Param("search") String search);

    Page<Pharmacie> findByStatut(StatutPharmacie statut, Pageable pageable);

    // ═══════════════════════════════════════════════════════════
    // GARDES
    // ═══════════════════════════════════════════════════════════

//    @Query("SELECT p FROM Pharmacie p WHERE p.syndicat.id = :syndicatId " +
//           "AND p.statut = 'ACTIVE' AND p.participeGardes = true ORDER BY p.nom")
//    List<Pharmacie> findPharmaciesGardeBySyndicat(@Param("syndicatId") UUID syndicatId);

//    @Query("SELECT p FROM Pharmacie p WHERE p.zone = :zone AND p.statut = 'ACTIVE' AND p.participeGardes = true")
//    List<Pharmacie> findByZone(@Param("zone") String zone);

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    long countByStatut(StatutPharmacie statut);

    long countBySyndicatId(UUID syndicatId);

    @Query("SELECT p.commune.id, COUNT(p) FROM Pharmacie p WHERE p.statut = 'ACTIVE' GROUP BY p.commune.id")
    List<Object[]> countByCommune();

    @Query("SELECT p.statut, COUNT(p) FROM Pharmacie p GROUP BY p.statut")
    List<Object[]> countByStatut();

    // ═══════════════════════════════════════════════════════════
    // AVEC RELATIONS
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT p FROM Pharmacie p JOIN FETCH p.commune c JOIN FETCH c.departement d JOIN FETCH d.region WHERE p.id = :id")
    Optional<Pharmacie> findByIdWithLocalisation(@Param("id") UUID id);

//    @Query("SELECT p FROM Pharmacie p LEFT JOIN FETCH p.titulaire WHERE p.id = :id")
//    Optional<Pharmacie> findByIdWithTitulaire(@Param("id") UUID id);

    // ═══════════════════════════════════════════════════════════════════════════════
// AJOUTER CES MÉTHODES À TON PharmacieRepository EXISTANT
// (Ne pas remplacer le fichier, juste copier ces méthodes dedans)
// ═══════════════════════════════════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR LOCALISATION AVEC STATUT (NOUVEAU)
    // ═══════════════════════════════════════════════════════════

    /**
     * Pharmacies par département et statut
     */
    @Query("SELECT p FROM Pharmacie p " +
            "WHERE p.commune.departement.id = :departementId " +
            "AND p.statut = :statut " +
            "ORDER BY p.nom")
    List<Pharmacie> findByDepartementAndStatut(
            @Param("departementId") UUID departementId,
            @Param("statut") StatutPharmacie statut);

    /**
     * Pharmacies par région et statut (toutes les localités)
     */
    @Query("SELECT p FROM Pharmacie p " +
            "WHERE p.commune.departement.region.id = :regionId " +
            "AND p.statut = :statut " +
            "ORDER BY p.nom")
    List<Pharmacie> findByRegionAndStatut(
            @Param("regionId") UUID regionId,
            @Param("statut") StatutPharmacie statut);

    // ═══════════════════════════════════════════════════════════
    // SECTEUR (SYNDICAT PAR COMMUNE)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouver le(s) syndicat(s) qui gèrent des pharmacies ACTIVE dans une commune donnée.
     */
    @Query("SELECT DISTINCT p.syndicat FROM Pharmacie p " +
            "WHERE p.commune.id = :communeId " +
            "AND p.statut = 'ACTIVE' " +
            "AND p.syndicat IS NOT NULL")
    List<sn.sunufarmasi.syndicat.entity.Syndicat> findSyndicatsByCommuneId(@Param("communeId") UUID communeId);

    /**
     * Toutes les pharmacies ACTIVE d'un syndicat.
     */
    List<Pharmacie> findBySyndicatIdAndStatut(UUID syndicatId, StatutPharmacie statut);

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES POUR SEED / ADMIN
    // ═══════════════════════════════════════════════════════════

    List<Pharmacie> findBySyndicatIsNull();

    boolean existsByTelephone(String telephone);

    boolean existsByNumeroOrdre(String numeroOrdre);

    List<Pharmacie> findBySyndicatId(UUID syndicatId);

    List<Pharmacie> findByStatut(StatutPharmacie statut);

    List<Pharmacie> findByNomContainingIgnoreCase(String nom);

    @Query("SELECT p FROM Pharmacie p WHERE p.pharmacienProprietaire.id = :pharmacienId")
    List<Pharmacie> findByPharmacienProprietaireId(@Param("pharmacienId") UUID pharmacienId);
}
