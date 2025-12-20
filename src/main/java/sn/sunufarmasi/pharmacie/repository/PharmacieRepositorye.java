package sn.sunufarmasi.pharmacie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Pharmacie
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface PharmacieRepositorye extends JpaRepository<Pharmacie, UUID> {

    /**
     * Trouver une pharmacie par code
     */
    Optional<Pharmacie> findByCode(String code);

    /**
     * Trouver les pharmacies d'un pharmacien
     */
    List<Pharmacie> findByPharmacienProprietaireId(UUID pharmacienId);

    /**
     * Trouver les pharmacies d'une commune
     */
    List<Pharmacie> findByCommuneId(UUID communeId);

    /**
     * Trouver les pharmacies d'un syndicat
     */
    List<Pharmacie> findBySyndicatId(UUID syndicatId);

    /**
     * Trouver les pharmacies sans syndicat
     */
    List<Pharmacie> findBySyndicatIsNull();

    /**
     * Trouver les pharmacies par statut
     */
    List<Pharmacie> findByStatut(StatutPharmacie statut);

    /**
     * Trouver les pharmacies en attente d'un syndicat
     */
    List<Pharmacie> findBySyndicatIsNullAndStatut(StatutPharmacie statut);

    /**
     * Trouver les pharmacies d'une région (via syndicat)
     */
    @Query("SELECT p FROM Pharmacie p WHERE p.syndicat.region.id = :regionId")
    List<Pharmacie> findBySyndicatRegionId(@Param("regionId") UUID regionId);

    /**
     * Trouver les pharmacies proches d'un point GPS (rayon en km)
     */
    @Query(value = """
        SELECT * FROM pharmacies p
        WHERE p.latitude IS NOT NULL 
        AND p.longitude IS NOT NULL
        AND p.statut = 'VALIDEE'
        AND (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(p.latitude)) *
                cos(radians(p.longitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(p.latitude))
            )
        ) <= :radiusKm
        ORDER BY (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(p.latitude)) *
                cos(radians(p.longitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(p.latitude))
            )
        )
        """, nativeQuery = true)
    List<Pharmacie> findNearby(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusKm") double radiusKm
    );

    /**
     * Rechercher des pharmacies par nom
     */
    List<Pharmacie> findByNomContainingIgnoreCase(String nom);

    /**
     * Vérifier si un code existe déjà
     */
    boolean existsByCode(String code);

    /**
     * Vérifier si un téléphone existe déjà
     */
    boolean existsByTelephone(String telephone);

    /**
     * Vérifier si un numéro d'ordre existe déjà
     */
    boolean existsByNumeroOrdre(String numeroOrdre);

    /**
     * Compter les pharmacies d'un pharmacien
     */
    long countByPharmacienProprietaireId(UUID pharmacienId);

    /**
     * Compter les pharmacies validées
     */
    long countByStatut(StatutPharmacie statut);
}