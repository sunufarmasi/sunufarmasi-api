package sn.sunufarmasi.localisation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.localisation.entity.Pays;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Pays
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface PaysRepository extends JpaRepository<Pays, UUID> {

    Optional<Pays> findByCode(String code);

    Optional<Pays> findByCodeIso2(String codeIso2);

    Optional<Pays> findByCodeIso3(String codeIso3);

    boolean existsByCode(String code);

    boolean existsByCodeIso2(String codeIso2);

    List<Pays> findByActifTrueOrderByNomAsc();

    @Query("SELECT p FROM Pays p WHERE LOWER(p.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.nomEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Pays> search(@Param("search") String search);

    @Query("SELECT p FROM Pays p LEFT JOIN FETCH p.regions WHERE p.id = :id")
    Optional<Pays> findByIdWithRegions(@Param("id") UUID id);

    @Query("SELECT p FROM Pays p LEFT JOIN FETCH p.regions WHERE p.code = :code")
    Optional<Pays> findByCodeWithRegions(@Param("code") String code);
}
