package sn.sunufarmasi.stock.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.stock.entity.MouvementStock;
import sn.sunufarmasi.stock.enums.TypeMouvement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité MouvementStock
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, UUID> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PHARMACIE
    // ═══════════════════════════════════════════════════════════

    List<MouvementStock> findByPharmacieId(UUID pharmacieId);

    Page<MouvementStock> findByPharmacieIdOrderByDateMouvementDesc(UUID pharmacieId, Pageable pageable);

    List<MouvementStock> findByPharmacieIdAndType(UUID pharmacieId, TypeMouvement type);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PRODUIT
    // ═══════════════════════════════════════════════════════════

    List<MouvementStock> findByProduitId(UUID produitId);

    List<MouvementStock> findByProduitPharmacieId(UUID produitPharmacieId);

    @Query("SELECT m FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.produit.id = :produitId ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findByPharmacieAndProduit(@Param("pharmacieId") UUID pharmacieId,
                                                    @Param("produitId") UUID produitId);

    @Query("SELECT m FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.produit.id = :produitId ORDER BY m.dateMouvement DESC")
    Page<MouvementStock> findByPharmacieAndProduit(@Param("pharmacieId") UUID pharmacieId,
                                                    @Param("produitId") UUID produitId,
                                                    Pageable pageable);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PÉRIODE
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT m FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.dateMouvement BETWEEN :debut AND :fin ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findByPharmacieAndPeriode(@Param("pharmacieId") UUID pharmacieId,
                                                    @Param("debut") LocalDateTime debut,
                                                    @Param("fin") LocalDateTime fin);

    @Query("SELECT m FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.type = :type AND m.dateMouvement BETWEEN :debut AND :fin " +
            "ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findByPharmacieAndTypeAndPeriode(@Param("pharmacieId") UUID pharmacieId,
                                                          @Param("type") TypeMouvement type,
                                                          @Param("debut") LocalDateTime debut,
                                                          @Param("fin") LocalDateTime fin);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR LOT
    // ═══════════════════════════════════════════════════════════

    List<MouvementStock> findByNumeroLot(String numeroLot);

    @Query("SELECT m FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.numeroLot = :numeroLot ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findByPharmacieAndLot(@Param("pharmacieId") UUID pharmacieId,
                                                @Param("numeroLot") String numeroLot);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR RÉFÉRENCE
    // ═══════════════════════════════════════════════════════════

    List<MouvementStock> findByReferenceExterne(String referenceExterne);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR UTILISATEUR
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT m FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.effectueParId = :userId ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findByPharmacieAndUser(@Param("pharmacieId") UUID pharmacieId,
                                                 @Param("userId") UUID userId);

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Total des entrées sur une période
     */
    @Query("SELECT SUM(m.quantite) FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.type IN ('ACHAT', 'RETOUR_CLIENT', 'TRANSFERT_ENTRANT', 'AJUSTEMENT_POSITIF', 'DON_RECU') " +
            "AND m.dateMouvement BETWEEN :debut AND :fin")
    Long sumEntrees(@Param("pharmacieId") UUID pharmacieId,
                    @Param("debut") LocalDateTime debut,
                    @Param("fin") LocalDateTime fin);

    /**
     * Total des sorties sur une période
     */
    @Query("SELECT SUM(m.quantite) FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.type IN ('VENTE', 'RETOUR_FOURNISSEUR', 'TRANSFERT_SORTANT', 'AJUSTEMENT_NEGATIF', 'PEREMPTION', 'CASSE', 'VOL', 'DON_EMIS') " +
            "AND m.dateMouvement BETWEEN :debut AND :fin")
    Long sumSorties(@Param("pharmacieId") UUID pharmacieId,
                    @Param("debut") LocalDateTime debut,
                    @Param("fin") LocalDateTime fin);

    /**
     * Valeur des achats sur une période
     */
    @Query("SELECT SUM(m.montantTotal) FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.type = 'ACHAT' AND m.dateMouvement BETWEEN :debut AND :fin")
    BigDecimal sumAchats(@Param("pharmacieId") UUID pharmacieId,
                         @Param("debut") LocalDateTime debut,
                         @Param("fin") LocalDateTime fin);

    /**
     * Valeur des ventes sur une période
     */
    @Query("SELECT SUM(m.montantTotal) FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.type = 'VENTE' AND m.dateMouvement BETWEEN :debut AND :fin")
    BigDecimal sumVentes(@Param("pharmacieId") UUID pharmacieId,
                         @Param("debut") LocalDateTime debut,
                         @Param("fin") LocalDateTime fin);

    /**
     * Compte par type de mouvement
     */
    @Query("SELECT m.type, COUNT(m), SUM(m.quantite) FROM MouvementStock m " +
            "WHERE m.pharmacie.id = :pharmacieId AND m.dateMouvement BETWEEN :debut AND :fin " +
            "GROUP BY m.type")
    List<Object[]> countByType(@Param("pharmacieId") UUID pharmacieId,
                                @Param("debut") LocalDateTime debut,
                                @Param("fin") LocalDateTime fin);

    /**
     * Pertes (péremption + casse + vol)
     */
    @Query("SELECT SUM(m.montantTotal) FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.type IN ('PEREMPTION', 'CASSE', 'VOL') " +
            "AND m.dateMouvement BETWEEN :debut AND :fin")
    BigDecimal sumPertes(@Param("pharmacieId") UUID pharmacieId,
                         @Param("debut") LocalDateTime debut,
                         @Param("fin") LocalDateTime fin);

    /**
     * Derniers mouvements d'un produit
     */
    @Query("SELECT m FROM MouvementStock m WHERE m.pharmacie.id = :pharmacieId " +
            "AND m.produit.id = :produitId ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findLastMouvements(@Param("pharmacieId") UUID pharmacieId,
                                             @Param("produitId") UUID produitId,
                                             Pageable pageable);
}
