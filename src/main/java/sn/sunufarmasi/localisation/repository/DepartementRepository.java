package sn.sunufarmasi.localisation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.localisation.entity.Departement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Departement
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface DepartementRepository extends JpaRepository<Departement, UUID> {

    Optional<Departement> findByCode(String code);

    Optional<Departement> findByCodeAndRegionId(String code, UUID regionId);

    boolean existsByCode(String code);

    boolean existsByCodeAndRegionId(String code, UUID regionId);

    List<Departement> findByRegionIdOrderByOrdreAscNomAsc(UUID regionId);

    List<Departement> findByRegionIdAndActifTrueOrderByOrdreAscNomAsc(UUID regionId);

    List<Departement> findByActifTrueOrderByNomAsc();

    @Query("SELECT d FROM Departement d WHERE d.region.pays.id = :paysId ORDER BY d.nom")
    List<Departement> findByPaysId(@Param("paysId") UUID paysId);

    @Query("SELECT d FROM Departement d WHERE LOWER(d.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Departement> search(@Param("search") String search);

    @Query("SELECT d FROM Departement d LEFT JOIN FETCH d.communes WHERE d.id = :id")
    Optional<Departement> findByIdWithCommunes(@Param("id") UUID id);

    @Query("SELECT d FROM Departement d JOIN FETCH d.region r JOIN FETCH r.pays WHERE d.id = :id")
    Optional<Departement> findByIdWithRegionAndPays(@Param("id") UUID id);

    @Query("SELECT d FROM Departement d JOIN FETCH d.region r JOIN FETCH r.pays " +
           "LEFT JOIN FETCH d.communes WHERE d.id = :id")
    Optional<Departement> findByIdWithAll(@Param("id") UUID id);

    @Query("SELECT COUNT(c) FROM Commune c WHERE c.departement.id = :departementId")
    Long countCommunesByDepartement(@Param("departementId") UUID departementId);

    @Query("SELECT d FROM Departement d WHERE d.region.code = :regionCode ORDER BY d.nom")
    List<Departement> findByRegionCode(@Param("regionCode") String regionCode);
}
