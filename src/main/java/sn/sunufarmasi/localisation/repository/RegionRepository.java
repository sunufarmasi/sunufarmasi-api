package sn.sunufarmasi.localisation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.localisation.entity.Region;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Region
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface RegionRepository extends JpaRepository<Region, UUID> {

    Optional<Region> findByCode(String code);

    Optional<Region> findByCodeAndPaysId(String code, UUID paysId);

    boolean existsByCode(String code);

    boolean existsByCodeAndPaysId(String code, UUID paysId);

    List<Region> findByPaysIdOrderByOrdreAscNomAsc(UUID paysId);

    List<Region> findByPaysIdAndActifTrueOrderByOrdreAscNomAsc(UUID paysId);

    List<Region> findByPaysCodeOrderByOrdreAscNomAsc(String paysCode);

    List<Region> findByActifTrueOrderByNomAsc();

    @Query("SELECT r FROM Region r WHERE LOWER(r.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Region> search(@Param("search") String search);

    @Query("SELECT r FROM Region r LEFT JOIN FETCH r.departements WHERE r.id = :id")
    Optional<Region> findByIdWithDepartements(@Param("id") UUID id);

    @Query("SELECT r FROM Region r JOIN FETCH r.pays WHERE r.id = :id")
    Optional<Region> findByIdWithPays(@Param("id") UUID id);

    @Query("SELECT r FROM Region r JOIN FETCH r.pays LEFT JOIN FETCH r.departements WHERE r.id = :id")
    Optional<Region> findByIdWithAll(@Param("id") UUID id);

    @Query("SELECT COUNT(d) FROM Departement d WHERE d.region.id = :regionId")
    Long countDepartementsByRegion(@Param("regionId") UUID regionId);

    @Query("SELECT COUNT(c) FROM Commune c WHERE c.departement.region.id = :regionId")
    Long countCommunesByRegion(@Param("regionId") UUID regionId);
}
