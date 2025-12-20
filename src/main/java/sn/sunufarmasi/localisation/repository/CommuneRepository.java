package sn.sunufarmasi.localisation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.enums.TypeCommune;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Commune
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface CommuneRepository extends JpaRepository<Commune, UUID> {

    Optional<Commune> findByCode(String code);

    Optional<Commune> findByCodeAndDepartementId(String code, UUID departementId);

    boolean existsByCode(String code);

    boolean existsByCodeAndDepartementId(String code, UUID departementId);

    List<Commune> findByDepartementIdOrderByOrdreAscNomAsc(UUID departementId);

    List<Commune> findByDepartementIdAndActifTrueOrderByOrdreAscNomAsc(UUID departementId);

    List<Commune> findByTypeOrderByNomAsc(TypeCommune type);

    List<Commune> findByActifTrueOrderByNomAsc();

    Page<Commune> findByActifTrue(Pageable pageable);

    @Query("SELECT c FROM Commune c WHERE c.departement.region.id = :regionId ORDER BY c.nom")
    List<Commune> findByRegionId(@Param("regionId") UUID regionId);

    @Query("SELECT c FROM Commune c WHERE c.departement.region.pays.id = :paysId ORDER BY c.nom")
    List<Commune> findByPaysId(@Param("paysId") UUID paysId);

    @Query("SELECT c FROM Commune c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.codePostal) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Commune> search(@Param("search") String search);

    @Query("SELECT c FROM Commune c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "AND c.actif = true ORDER BY c.nom")
    List<Commune> searchActives(@Param("search") String search);

    @Query("SELECT c FROM Commune c JOIN FETCH c.departement d JOIN FETCH d.region r " +
           "JOIN FETCH r.pays WHERE c.id = :id")
    Optional<Commune> findByIdWithAll(@Param("id") UUID id);

    @Query("SELECT c FROM Commune c WHERE c.codePostal = :codePostal")
    List<Commune> findByCodePostal(@Param("codePostal") String codePostal);

    // ═══════════════════════════════════════════════════════════
    // REQUÊTES GÉOGRAPHIQUES
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT c FROM Commune c WHERE c.zoneUrbaine = true ORDER BY c.nom")
    List<Commune> findZonesUrbaines();

    @Query("SELECT c FROM Commune c WHERE c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<Commune> findWithCoordinates();

    /**
     * Recherche par proximité (approximation sans calcul Haversine en JPQL)
     * Pour une recherche précise, utiliser une requête native ou post-filtrer
     */
    @Query("""
        SELECT c FROM Commune c 
        WHERE c.latitude BETWEEN :minLat AND :maxLat 
        AND c.longitude BETWEEN :minLng AND :maxLng
        AND c.actif = true
        ORDER BY c.nom
        """)
    List<Commune> findInBoundingBox(
        @Param("minLat") Double minLat,
        @Param("maxLat") Double maxLat,
        @Param("minLng") Double minLng,
        @Param("maxLng") Double maxLng
    );

    /**
     * Recherche par proximité avec distance (requête native PostgreSQL)
     * Utilise la formule Haversine pour calculer la distance
     */
    @Query(value = """
        SELECT c.*, 
            (6371 * acos(cos(radians(:lat)) * cos(radians(c.latitude)) 
            * cos(radians(c.longitude) - radians(:lng)) 
            + sin(radians(:lat)) * sin(radians(c.latitude)))) AS distance
        FROM communes c
        WHERE c.actif = true
        AND c.latitude IS NOT NULL 
        AND c.longitude IS NOT NULL
        HAVING distance < :rayonKm
        ORDER BY distance
        LIMIT :limit
        """, nativeQuery = true)
    List<Commune> findNearbyNative(
        @Param("lat") Double lat,
        @Param("lng") Double lng,
        @Param("rayonKm") Double rayonKm,
        @Param("limit") Integer limit
    );

    @Query("SELECT c FROM Commune c WHERE c.departement.code = :departementCode ORDER BY c.nom")
    List<Commune> findByDepartementCode(@Param("departementCode") String departementCode);

    @Query("SELECT c FROM Commune c WHERE c.departement.region.code = :regionCode ORDER BY c.nom")
    List<Commune> findByRegionCode(@Param("regionCode") String regionCode);
}
