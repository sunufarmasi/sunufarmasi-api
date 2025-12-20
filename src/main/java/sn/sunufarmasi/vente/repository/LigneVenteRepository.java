package sn.sunufarmasi.vente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.vente.entity.LigneVente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité LigneVente
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface LigneVenteRepository extends JpaRepository<LigneVente, UUID> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR VENTE
    // ═══════════════════════════════════════════════════════════

    List<LigneVente> findByVenteId(UUID venteId);

    List<LigneVente> findByVenteIdOrderByNumeroLigne(UUID venteId);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PRODUIT
    // ═══════════════════════════════════════════════════════════

    List<LigneVente> findByProduitId(UUID produitId);

    @Query("SELECT lv FROM LigneVente lv JOIN lv.vente v " +
            "WHERE v.pharmacie.id = :pharmacieId AND lv.produit.id = :produitId " +
            "AND v.statut = 'VALIDEE' ORDER BY v.dateVente DESC")
    List<LigneVente> findByPharmacieAndProduit(@Param("pharmacieId") UUID pharmacieId,
                                                @Param("produitId") UUID produitId);

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES PRODUITS
    // ═══════════════════════════════════════════════════════════

    /**
     * Top produits vendus (quantité)
     */
    @Query("SELECT lv.produit.id, lv.produitNom, SUM(lv.quantite), SUM(lv.montantTTC) " +
            "FROM LigneVente lv JOIN lv.vente v " +
            "WHERE v.pharmacie.id = :pharmacieId AND v.statut = 'VALIDEE' " +
            "AND v.dateVente BETWEEN :debut AND :fin " +
            "GROUP BY lv.produit.id, lv.produitNom ORDER BY SUM(lv.quantite) DESC")
    List<Object[]> topProduitsParQuantite(@Param("pharmacieId") UUID pharmacieId,
                                           @Param("debut") LocalDateTime debut,
                                           @Param("fin") LocalDateTime fin);

    /**
     * Top produits vendus (CA)
     */
    @Query("SELECT lv.produit.id, lv.produitNom, SUM(lv.quantite), SUM(lv.montantTTC) " +
            "FROM LigneVente lv JOIN lv.vente v " +
            "WHERE v.pharmacie.id = :pharmacieId AND v.statut = 'VALIDEE' " +
            "AND v.dateVente BETWEEN :debut AND :fin " +
            "GROUP BY lv.produit.id, lv.produitNom ORDER BY SUM(lv.montantTTC) DESC")
    List<Object[]> topProduitsParCA(@Param("pharmacieId") UUID pharmacieId,
                                     @Param("debut") LocalDateTime debut,
                                     @Param("fin") LocalDateTime fin);

    /**
     * Ventes d'un produit sur une période
     */
    @Query("SELECT SUM(lv.quantite), SUM(lv.montantTTC) FROM LigneVente lv JOIN lv.vente v " +
            "WHERE v.pharmacie.id = :pharmacieId AND lv.produit.id = :produitId " +
            "AND v.statut = 'VALIDEE' AND v.dateVente BETWEEN :debut AND :fin")
    List<Object[]> statsProduit(@Param("pharmacieId") UUID pharmacieId,
                                 @Param("produitId") UUID produitId,
                                 @Param("debut") LocalDateTime debut,
                                 @Param("fin") LocalDateTime fin);

    /**
     * CA total par produit
     */
    @Query("SELECT SUM(lv.montantTTC) FROM LigneVente lv JOIN lv.vente v " +
            "WHERE v.pharmacie.id = :pharmacieId AND lv.produit.id = :produitId " +
            "AND v.statut = 'VALIDEE' AND v.dateVente BETWEEN :debut AND :fin")
    BigDecimal sumCAProduit(@Param("pharmacieId") UUID pharmacieId,
                            @Param("produitId") UUID produitId,
                            @Param("debut") LocalDateTime debut,
                            @Param("fin") LocalDateTime fin);

    /**
     * Quantité totale vendue d'un produit
     */
    @Query("SELECT COALESCE(SUM(lv.quantite), 0) FROM LigneVente lv JOIN lv.vente v " +
            "WHERE v.pharmacie.id = :pharmacieId AND lv.produit.id = :produitId " +
            "AND v.statut = 'VALIDEE' AND v.dateVente BETWEEN :debut AND :fin")
    Integer sumQuantiteVendue(@Param("pharmacieId") UUID pharmacieId,
                               @Param("produitId") UUID produitId,
                               @Param("debut") LocalDateTime debut,
                               @Param("fin") LocalDateTime fin);
}
