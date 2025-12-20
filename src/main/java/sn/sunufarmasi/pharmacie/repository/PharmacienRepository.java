package sn.sunufarmasi.pharmacie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Pharmacien
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface PharmacienRepository extends JpaRepository<Pharmacien, UUID> {

    /**
     * Trouver un pharmacien par téléphone
     */
    Optional<Pharmacien> findByTelephone(String telephone);

    /**
     * Trouver un pharmacien par email
     */
    Optional<Pharmacien> findByEmail(String email);

    /**
     * Trouver un pharmacien par numéro d'ordre national
     */
    Optional<Pharmacien> findByNumeroOrdreNational(String numeroOrdreNational);

    /**
     * Vérifier si un téléphone existe déjà
     */
    boolean existsByTelephone(String telephone);

    /**
     * Vérifier si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Vérifier si un numéro d'ordre existe déjà
     */
    boolean existsByNumeroOrdreNational(String numeroOrdreNational);
}