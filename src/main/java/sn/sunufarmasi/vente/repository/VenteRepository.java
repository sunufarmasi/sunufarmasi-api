package sn.sunufarmasi.vente.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.vente.entity.Vente;
import sn.sunufarmasi.vente.enums.ModePaiement;
import sn.sunufarmasi.vente.enums.StatutVente;
import sn.sunufarmasi.vente.enums.TypeVente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Vente
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface VenteRepository extends JpaRepository<Vente, UUID> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR IDENTIFIANTS
    // ═══════════════════════════════════════════════════════════

    Optional<Vente> findByNumero(String numero);

    Optional<Vente> findByNumeroTicket(String numeroTicket);

    boolean existsByNumero(String numero);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PHARMACIE
    // ═══════════════════════════════════════════════════════════

    List<Vente> findByPharmacieId(UUID pharmacieId);

    Page<Vente> findByPharmacieIdOrderByDateVenteDesc(UUID pharmacieId, Pageable pageable);

    List<Vente> findByPharmacieIdAndStatut(UUID pharmacieId, StatutVente statut);

    List<Vente> findByPharmacieIdAndType(UUID pharmacieId, TypeVente type);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PÉRIODE
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT v FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.dateVente BETWEEN :debut AND :fin ORDER BY v.dateVente DESC")
    List<Vente> findByPharmacieAndPeriode(@Param("pharmacieId") UUID pharmacieId,
                                           @Param("debut") LocalDateTime debut,
                                           @Param("fin") LocalDateTime fin);

    @Query("SELECT v FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.statut = :statut AND v.dateVente BETWEEN :debut AND :fin " +
            "ORDER BY v.dateVente DESC")
    List<Vente> findByPharmacieAndStatutAndPeriode(@Param("pharmacieId") UUID pharmacieId,
                                                    @Param("statut") StatutVente statut,
                                                    @Param("debut") LocalDateTime debut,
                                                    @Param("fin") LocalDateTime fin);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR CLIENT
    // ═══════════════════════════════════════════════════════════

    List<Vente> findByClientId(UUID clientId);

    @Query("SELECT v FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.clientId = :clientId ORDER BY v.dateVente DESC")
    List<Vente> findByPharmacieAndClient(@Param("pharmacieId") UUID pharmacieId,
                                          @Param("clientId") UUID clientId);

    @Query("SELECT v FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND (LOWER(v.clientNom) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR v.clientTelephone LIKE CONCAT('%', :search, '%')) " +
            "ORDER BY v.dateVente DESC")
    List<Vente> searchByClient(@Param("pharmacieId") UUID pharmacieId, @Param("search") String search);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR VENDEUR
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT v FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.vendeurId = :vendeurId ORDER BY v.dateVente DESC")
    List<Vente> findByPharmacieAndVendeur(@Param("pharmacieId") UUID pharmacieId,
                                           @Param("vendeurId") UUID vendeurId);

    @Query("SELECT v FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.vendeurId = :vendeurId AND v.dateVente BETWEEN :debut AND :fin " +
            "ORDER BY v.dateVente DESC")
    List<Vente> findByPharmacieAndVendeurAndPeriode(@Param("pharmacieId") UUID pharmacieId,
                                                     @Param("vendeurId") UUID vendeurId,
                                                     @Param("debut") LocalDateTime debut,
                                                     @Param("fin") LocalDateTime fin);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE MUTUELLES
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT v FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.mutuelleId = :mutuelleId ORDER BY v.dateVente DESC")
    List<Vente> findByPharmacieAndMutuelle(@Param("pharmacieId") UUID pharmacieId,
                                            @Param("mutuelleId") UUID mutuelleId);

    @Query("SELECT v FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.type IN ('MUTUELLE', 'TIERS_PAYANT') " +
            "AND v.dateVente BETWEEN :debut AND :fin ORDER BY v.dateVente DESC")
    List<Vente> findVentesMutuelle(@Param("pharmacieId") UUID pharmacieId,
                                    @Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin);

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Chiffre d'affaires sur une période
     */
    @Query("SELECT COALESCE(SUM(v.montantNetTTC), 0) FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.statut = 'VALIDEE' AND v.dateVente BETWEEN :debut AND :fin")
    BigDecimal sumChiffreAffaires(@Param("pharmacieId") UUID pharmacieId,
                                   @Param("debut") LocalDateTime debut,
                                   @Param("fin") LocalDateTime fin);

    /**
     * Nombre de ventes sur une période
     */
    @Query("SELECT COUNT(v) FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.statut = 'VALIDEE' AND v.dateVente BETWEEN :debut AND :fin")
    Long countVentes(@Param("pharmacieId") UUID pharmacieId,
                     @Param("debut") LocalDateTime debut,
                     @Param("fin") LocalDateTime fin);

    /**
     * Panier moyen
     */
    @Query("SELECT COALESCE(AVG(v.montantNetTTC), 0) FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.statut = 'VALIDEE' AND v.dateVente BETWEEN :debut AND :fin")
    BigDecimal avgPanierMoyen(@Param("pharmacieId") UUID pharmacieId,
                               @Param("debut") LocalDateTime debut,
                               @Param("fin") LocalDateTime fin);

    /**
     * Total des remises
     */
    @Query("SELECT COALESCE(SUM(v.montantRemise), 0) FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.statut = 'VALIDEE' AND v.dateVente BETWEEN :debut AND :fin")
    BigDecimal sumRemises(@Param("pharmacieId") UUID pharmacieId,
                          @Param("debut") LocalDateTime debut,
                          @Param("fin") LocalDateTime fin);

    /**
     * Montant total mutuelle
     */
    @Query("SELECT COALESCE(SUM(v.montantMutuelle), 0) FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.statut = 'VALIDEE' AND v.dateVente BETWEEN :debut AND :fin")
    BigDecimal sumMontantMutuelle(@Param("pharmacieId") UUID pharmacieId,
                                   @Param("debut") LocalDateTime debut,
                                   @Param("fin") LocalDateTime fin);

    /**
     * Ventes par mode de paiement
     */
    @Query("SELECT v.modePaiement, COUNT(v), SUM(v.montantPaye) FROM Vente v " +
            "WHERE v.pharmacie.id = :pharmacieId AND v.statut = 'VALIDEE' " +
            "AND v.dateVente BETWEEN :debut AND :fin GROUP BY v.modePaiement")
    List<Object[]> statsByModePaiement(@Param("pharmacieId") UUID pharmacieId,
                                        @Param("debut") LocalDateTime debut,
                                        @Param("fin") LocalDateTime fin);

    /**
     * Ventes par type
     */
    @Query("SELECT v.type, COUNT(v), SUM(v.montantNetTTC) FROM Vente v " +
            "WHERE v.pharmacie.id = :pharmacieId AND v.statut = 'VALIDEE' " +
            "AND v.dateVente BETWEEN :debut AND :fin GROUP BY v.type")
    List<Object[]> statsByType(@Param("pharmacieId") UUID pharmacieId,
                                @Param("debut") LocalDateTime debut,
                                @Param("fin") LocalDateTime fin);

    /**
     * Ventes par vendeur
     */
    @Query("SELECT v.vendeurId, v.vendeurNom, COUNT(v), SUM(v.montantNetTTC) FROM Vente v " +
            "WHERE v.pharmacie.id = :pharmacieId AND v.statut = 'VALIDEE' " +
            "AND v.dateVente BETWEEN :debut AND :fin GROUP BY v.vendeurId, v.vendeurNom")
    List<Object[]> statsByVendeur(@Param("pharmacieId") UUID pharmacieId,
                                   @Param("debut") LocalDateTime debut,
                                   @Param("fin") LocalDateTime fin);

    /**
     * CA par jour sur une période
     */
    @Query("SELECT CAST(v.dateVente AS date), COUNT(v), SUM(v.montantNetTTC) FROM Vente v " +
            "WHERE v.pharmacie.id = :pharmacieId AND v.statut = 'VALIDEE' " +
            "AND v.dateVente BETWEEN :debut AND :fin " +
            "GROUP BY CAST(v.dateVente AS date) ORDER BY CAST(v.dateVente AS date)")
    List<Object[]> statsParJour(@Param("pharmacieId") UUID pharmacieId,
                                 @Param("debut") LocalDateTime debut,
                                 @Param("fin") LocalDateTime fin);

    // ═══════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Compter les ventes du jour pour numérotation
     */
    @Query("SELECT COUNT(v) FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND CAST(v.dateVente AS date) = CAST(:date AS date)")
    Long countVentesDuJour(@Param("pharmacieId") UUID pharmacieId, @Param("date") LocalDateTime date);

    /**
     * Ventes non soldées (crédit)
     */
    @Query("SELECT v FROM Vente v WHERE v.pharmacie.id = :pharmacieId " +
            "AND v.statut = 'PARTIELLEMENT_PAYEE' ORDER BY v.dateVente")
    List<Vente> findVentesNonSoldees(@Param("pharmacieId") UUID pharmacieId);
}
