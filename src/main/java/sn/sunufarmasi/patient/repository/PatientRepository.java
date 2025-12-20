package sn.sunufarmasi.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.patient.entity.Patient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Patient
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    /**
     * Trouver un patient par email
     */
    Optional<Patient> findByEmail(String email);

    /**
     * Trouver un patient par téléphone
     */
    Optional<Patient> findByTelephone(String telephone);

    /**
     * Trouver un patient par email ou téléphone
     */
    @Query("SELECT p FROM Patient p WHERE p.email = :identifier OR p.telephone = :identifier")
    Optional<Patient> findByEmailOrTelephone(@Param("identifier") String identifier);

    /**
     * Vérifier si un email existe
     */
    boolean existsByEmail(String email);

    /**
     * Vérifier si un téléphone existe
     */
    boolean existsByTelephone(String telephone);

    /**
     * Trouver tous les patients actifs
     */
    List<Patient> findByActifTrue();

    /**
     * Trouver les patients d'une commune
     */
    List<Patient> findByCommuneIdAndActifTrue(UUID communeId);

    /**
     * Rechercher des patients par nom
     */
    @Query("SELECT p FROM Patient p WHERE LOWER(p.nomComplet) LIKE LOWER(CONCAT('%', :nom, '%')) AND p.actif = true")
    List<Patient> searchByNom(@Param("nom") String nom);

    /**
     * Trouver les patients connectés récemment
     */
    @Query("SELECT p FROM Patient p WHERE p.lastLoginAt > :since AND p.actif = true ORDER BY p.lastLoginAt DESC")
    List<Patient> findRecentlyActive(@Param("since") LocalDateTime since);

    /**
     * Compter les patients actifs
     */
    long countByActifTrue();

    /**
     * Compter les patients vérifiés
     */
    long countByEmailVerifiedTrueAndActifTrue();
}