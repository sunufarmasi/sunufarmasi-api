package sn.sunufarmasi.mutuelle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.mutuelle.entity.Mutuelle;
import sn.sunufarmasi.mutuelle.enums.TypeMutuelle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MutuelleRepository extends JpaRepository<Mutuelle, UUID> {

    Optional<Mutuelle> findByCode(String code);

    boolean existsByCode(String code);

    List<Mutuelle> findByEstActif(boolean estActif);

    List<Mutuelle> findByType(TypeMutuelle type);

    List<Mutuelle> findByTypeAndEstActif(TypeMutuelle type, boolean estActif);

    @Query("SELECT m FROM Mutuelle m WHERE m.estActif = true " +
            "AND (LOWER(m.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(m.nomCourt) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR m.code = :search)")
    List<Mutuelle> search(@Param("search") String search);

    @Query("SELECT DISTINCT m FROM Mutuelle m " +
            "JOIN ContratMutuelle c ON c.mutuelle = m " +
            "WHERE c.pharmacie.id = :pharmacieId AND c.estActif = true AND m.estActif = true")
    List<Mutuelle> findMutuellesConventionnees(@Param("pharmacieId") UUID pharmacieId);
}
