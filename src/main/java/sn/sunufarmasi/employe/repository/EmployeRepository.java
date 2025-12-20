package sn.sunufarmasi.employe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.employe.entity.Employe;
import sn.sunufarmasi.employe.enums.StatutEmploye;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Employe
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface EmployeRepository extends JpaRepository<Employe, UUID> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR IDENTIFIANTS UNIQUES
    // ═══════════════════════════════════════════════════════════

    Optional<Employe> findByCode(String code);

    Optional<Employe> findByUsername(String username);

    boolean existsByCode(String code);

    boolean existsByUsername(String username);

    boolean existsByTelephone(String telephone);

    boolean existsByEmail(String email);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PHARMACIE
    // ═══════════════════════════════════════════════════════════

    List<Employe> findByPharmacieId(UUID pharmacieId);

    List<Employe> findByPharmacieIdAndStatut(UUID pharmacieId, StatutEmploye statut);

    Long countByPharmacieId(UUID pharmacieId);

    Long countByPharmacieIdAndStatut(UUID pharmacieId, StatutEmploye statut);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PHARMACIEN
    // ═══════════════════════════════════════════════════════════

    List<Employe> findByPharmacienId(UUID pharmacienId);

    List<Employe> findByPharmacienIdAndStatut(UUID pharmacienId, StatutEmploye statut);

    Long countByPharmacienId(UUID pharmacienId);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR STATUT
    // ═══════════════════════════════════════════════════════════

    List<Employe> findByStatut(StatutEmploye statut);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR NOM
    // ═══════════════════════════════════════════════════════════

    List<Employe> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);

    @Query("SELECT e FROM Employe e WHERE e.pharmacie.id = :pharmacieId " +
            "AND (LOWER(e.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Employe> searchByPharmacieAndNom(@Param("pharmacieId") UUID pharmacieId, @Param("search") String search);

    // ═══════════════════════════════════════════════════════════
    // REQUÊTES PERSONNALISÉES
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouver un employé actif par username (pour login)
     */
    @Query("SELECT e FROM Employe e WHERE e.username = :username AND e.statut = 'ACTIF'")
    Optional<Employe> findActiveByUsername(@Param("username") String username);

    /**
     * Vérifier si un employé appartient à un pharmacien
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Employe e WHERE e.id = :employeId AND e.pharmacien.id = :pharmacienId")
    boolean belongsToPharmacien(@Param("employeId") UUID employeId, @Param("pharmacienId") UUID pharmacienId);

    /**
     * Vérifier si un employé appartient à une pharmacie
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Employe e WHERE e.id = :employeId AND e.pharmacie.id = :pharmacieId")
    boolean belongsToPharmacie(@Param("employeId") UUID employeId, @Param("pharmacieId") UUID pharmacieId);

    /**
     * Compter les employés actifs d'une pharmacie
     */
    @Query("SELECT COUNT(e) FROM Employe e WHERE e.pharmacie.id = :pharmacieId AND e.statut = 'ACTIF'")
    Long countActifsByPharmacie(@Param("pharmacieId") UUID pharmacieId);

    /**
     * Trouver les employés connectés récemment (dernières 24h)
     */
    @Query("SELECT e FROM Employe e WHERE e.pharmacie.id = :pharmacieId " +
            "AND e.derniereConnexion >= :depuis ORDER BY e.derniereConnexion DESC")
    List<Employe> findRecentlyConnected(@Param("pharmacieId") UUID pharmacieId, @Param("depuis") LocalDateTime depuis);

    /**
     * Trouver les employés par poste
     */
    List<Employe> findByPharmacieIdAndPosteContainingIgnoreCase(UUID pharmacieId, String poste);

    /**
     * Compter le nombre total d'employés par pharmacien (toutes ses pharmacies)
     */
    @Query("SELECT COUNT(e) FROM Employe e WHERE e.pharmacien.id = :pharmacienId AND e.statut = 'ACTIF'")
    Long countActifsByPharmacien(@Param("pharmacienId") UUID pharmacienId);
}
