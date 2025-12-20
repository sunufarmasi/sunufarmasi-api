package sn.sunufarmasi.mutuelle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.mutuelle.entity.ContratMutuelle;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContratMutuelleRepository extends JpaRepository<ContratMutuelle, UUID> {

    List<ContratMutuelle> findByPharmacieId(UUID pharmacieId);

    List<ContratMutuelle> findByMutuelleId(UUID mutuelleId);

    Optional<ContratMutuelle> findByPharmacieIdAndMutuelleId(UUID pharmacieId, UUID mutuelleId);

    @Query("SELECT c FROM ContratMutuelle c WHERE c.pharmacie.id = :pharmacieId " +
            "AND c.estActif = true AND c.dateDebut <= :date " +
            "AND (c.dateFin IS NULL OR c.dateFin >= :date)")
    List<ContratMutuelle> findContratsActifs(@Param("pharmacieId") UUID pharmacieId,
                                              @Param("date") LocalDate date);

    @Query("SELECT c FROM ContratMutuelle c WHERE c.pharmacie.id = :pharmacieId " +
            "AND c.mutuelle.id = :mutuelleId AND c.estActif = true " +
            "AND c.dateDebut <= :date AND (c.dateFin IS NULL OR c.dateFin >= :date)")
    Optional<ContratMutuelle> findContratValide(@Param("pharmacieId") UUID pharmacieId,
                                                 @Param("mutuelleId") UUID mutuelleId,
                                                 @Param("date") LocalDate date);
}
