package sn.sunufarmasi.mutuelle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.mutuelle.entity.Adherent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdherentRepository extends JpaRepository<Adherent, UUID> {

    List<Adherent> findByMutuelleId(UUID mutuelleId);

    Optional<Adherent> findByMutuelleIdAndNumeroAdherent(UUID mutuelleId, String numeroAdherent);

    Optional<Adherent> findByNumeroCarte(String numeroCarte);

    @Query("SELECT a FROM Adherent a WHERE a.mutuelle.id = :mutuelleId " +
            "AND a.estActif = true AND (LOWER(a.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(a.prenom) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR a.numeroAdherent = :search)")
    List<Adherent> search(@Param("mutuelleId") UUID mutuelleId, @Param("search") String search);

    @Query("SELECT a FROM Adherent a WHERE a.estActif = true " +
            "AND (a.numeroAdherent = :numero OR a.numeroCarte = :numero)")
    Optional<Adherent> findByNumero(@Param("numero") String numero);

    List<Adherent> findByAdherentPrincipalId(UUID adherentPrincipalId);

    @Query("SELECT a FROM Adherent a WHERE a.mutuelle.id = :mutuelleId " +
            "AND a.estActif = true AND a.dateFinDroits IS NOT NULL " +
            "AND a.dateFinDroits < CURRENT_DATE")
    List<Adherent> findDroitsExpires(@Param("mutuelleId") UUID mutuelleId);
}
