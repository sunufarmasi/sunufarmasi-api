package sn.sunufarmasi.stock.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.stock.entity.ProduitPharmacie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité ProduitPharmacie (stock par pharmacie)
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface ProduitPharmacieRepository extends JpaRepository<ProduitPharmacie, UUID> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PHARMACIE
    // ═══════════════════════════════════════════════════════════

    List<ProduitPharmacie> findByPharmacieId(UUID pharmacieId);

    Page<ProduitPharmacie> findByPharmacieId(UUID pharmacieId, Pageable pageable);

    List<ProduitPharmacie> findByPharmacieIdAndEstActif(UUID pharmacieId, boolean estActif);

    Optional<ProduitPharmacie> findByPharmacieIdAndProduitId(UUID pharmacieId, UUID produitId);

    boolean existsByPharmacieIdAndProduitId(UUID pharmacieId, UUID produitId);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PRODUIT
    // ═══════════════════════════════════════════════════════════

    List<ProduitPharmacie> findByProduitId(UUID produitId);

    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.produit.code = :code AND pp.pharmacie.id = :pharmacieId")
    Optional<ProduitPharmacie> findByPharmacieIdAndProduitCode(@Param("pharmacieId") UUID pharmacieId,
                                                                @Param("code") String code);

    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.produit.codeBarre = :codeBarre AND pp.pharmacie.id = :pharmacieId")
    Optional<ProduitPharmacie> findByPharmacieIdAndCodeBarre(@Param("pharmacieId") UUID pharmacieId,
                                                             @Param("codeBarre") String codeBarre);

    // ═══════════════════════════════════════════════════════════
    // ALERTES STOCK
    // ═══════════════════════════════════════════════════════════

    /**
     * Produits en rupture de stock
     */
    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.estActif = true AND pp.quantiteStock <= 0")
    List<ProduitPharmacie> findEnRupture(@Param("pharmacieId") UUID pharmacieId);

    /**
     * Produits avec stock bas (sous seuil alerte)
     */
    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.estActif = true AND pp.quantiteStock > 0 AND pp.quantiteStock <= pp.seuilAlerte")
    List<ProduitPharmacie> findStockBas(@Param("pharmacieId") UUID pharmacieId);

    /**
     * Produits à réapprovisionner (sous seuil réappro)
     */
    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.estActif = true AND pp.quantiteStock <= pp.seuilReappro")
    List<ProduitPharmacie> findAReapprovisionner(@Param("pharmacieId") UUID pharmacieId);

    // ═══════════════════════════════════════════════════════════
    // ALERTES PÉREMPTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Produits périmés
     */
    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.datePeremptionProche < :aujourdhui AND pp.quantiteStock > 0")
    List<ProduitPharmacie> findPerimes(@Param("pharmacieId") UUID pharmacieId,
                                        @Param("aujourdhui") LocalDate aujourdhui);

    /**
     * Produits bientôt périmés
     */
    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.datePeremptionProche BETWEEN :aujourdhui AND :dateLimite " +
            "AND pp.quantiteStock > 0")
    List<ProduitPharmacie> findBientotPerimes(@Param("pharmacieId") UUID pharmacieId,
                                               @Param("aujourdhui") LocalDate aujourdhui,
                                               @Param("dateLimite") LocalDate dateLimite);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PRODUITS
    // ═══════════════════════════════════════════════════════════

    /**
     * Recherche par nom/DCI dans une pharmacie
     */
    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.estActif = true " +
            "AND (LOWER(pp.produit.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(pp.produit.dci) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ProduitPharmacie> searchInPharmacie(@Param("pharmacieId") UUID pharmacieId,
                                              @Param("search") String search);

    /**
     * Recherche avec pagination
     */
    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.estActif = true " +
            "AND (LOWER(pp.produit.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(pp.produit.dci) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProduitPharmacie> searchInPharmacie(@Param("pharmacieId") UUID pharmacieId,
                                              @Param("search") String search,
                                              Pageable pageable);

    /**
     * Recherche par emplacement
     */
    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND (pp.emplacement LIKE CONCAT('%', :emplacement, '%') " +
            "OR pp.rayon = :emplacement OR pp.etagere = :emplacement)")
    List<ProduitPharmacie> findByEmplacement(@Param("pharmacieId") UUID pharmacieId,
                                              @Param("emplacement") String emplacement);

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Nombre total de produits dans une pharmacie
     */
    @Query("SELECT COUNT(pp) FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId AND pp.estActif = true")
    Long countByPharmacie(@Param("pharmacieId") UUID pharmacieId);

    /**
     * Valeur totale du stock
     */
    @Query("SELECT SUM(pp.prixAchatHT * pp.quantiteStock) FROM ProduitPharmacie pp " +
            "WHERE pp.pharmacie.id = :pharmacieId AND pp.estActif = true AND pp.quantiteStock > 0")
    BigDecimal calculerValeurStock(@Param("pharmacieId") UUID pharmacieId);

    /**
     * Nombre de produits en rupture
     */
    @Query("SELECT COUNT(pp) FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.estActif = true AND pp.quantiteStock <= 0")
    Long countEnRupture(@Param("pharmacieId") UUID pharmacieId);

    /**
     * Nombre de produits avec stock bas
     */
    @Query("SELECT COUNT(pp) FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.estActif = true AND pp.quantiteStock > 0 AND pp.quantiteStock <= pp.seuilAlerte")
    Long countStockBas(@Param("pharmacieId") UUID pharmacieId);

    /**
     * Nombre de produits périmés ou bientôt périmés
     */
    @Query("SELECT COUNT(pp) FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.datePeremptionProche <= :dateLimite AND pp.quantiteStock > 0")
    Long countProchesPeremption(@Param("pharmacieId") UUID pharmacieId,
                                 @Param("dateLimite") LocalDate dateLimite);

    /**
     * Top produits vendus
     */
    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.estActif = true ORDER BY pp.totalVendu DESC")
    List<ProduitPharmacie> findTopVendus(@Param("pharmacieId") UUID pharmacieId, Pageable pageable);

    /**
     * Produits sans mouvement depuis X jours
     */
    @Query("SELECT pp FROM ProduitPharmacie pp WHERE pp.pharmacie.id = :pharmacieId " +
            "AND pp.estActif = true AND pp.quantiteStock > 0 " +
            "AND (pp.derniereVente IS NULL OR pp.derniereVente < :dateLimite)")
    List<ProduitPharmacie> findSansMouvement(@Param("pharmacieId") UUID pharmacieId,
                                              @Param("dateLimite") java.time.LocalDateTime dateLimite);
}
