package sn.sunufarmasi.stock.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.stock.entity.Produit;
import sn.sunufarmasi.stock.enums.CategorieProduit;
import sn.sunufarmasi.stock.enums.FormeProduit;
import sn.sunufarmasi.stock.enums.StatutProduit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Produit (catalogue national)
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface ProduitRepository extends JpaRepository<Produit, UUID> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR IDENTIFIANTS
    // ═══════════════════════════════════════════════════════════

    Optional<Produit> findByCode(String code);

    Optional<Produit> findByCodeBarre(String codeBarre);

    Optional<Produit> findByCodeCip(String codeCip);

    boolean existsByCode(String code);

    boolean existsByCodeBarre(String codeBarre);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR NOM / DCI
    // ═══════════════════════════════════════════════════════════

    List<Produit> findByNomContainingIgnoreCase(String nom);

    List<Produit> findByDciContainingIgnoreCase(String dci);

    @Query("SELECT p FROM Produit p WHERE " +
            "LOWER(p.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.dci) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "p.codeBarre = :search OR " +
            "p.code = :search")
    List<Produit> searchByNomOrDciOrCode(@Param("search") String search);

    @Query("SELECT p FROM Produit p WHERE " +
            "(LOWER(p.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.dci) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND p.statut = 'ACTIF'")
    Page<Produit> searchActifs(@Param("search") String search, Pageable pageable);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR CATÉGORIE / FORME
    // ═══════════════════════════════════════════════════════════

    List<Produit> findByCategorie(CategorieProduit categorie);

    List<Produit> findByForme(FormeProduit forme);

    List<Produit> findByCategorieAndStatut(CategorieProduit categorie, StatutProduit statut);

    List<Produit> findByFormeAndStatut(FormeProduit forme, StatutProduit statut);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR LABORATOIRE
    // ═══════════════════════════════════════════════════════════

    List<Produit> findByLaboratoireContainingIgnoreCase(String laboratoire);

    @Query("SELECT DISTINCT p.laboratoire FROM Produit p WHERE p.laboratoire IS NOT NULL ORDER BY p.laboratoire")
    List<String> findAllLaboratoires();

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR STATUT
    // ═══════════════════════════════════════════════════════════

    List<Produit> findByStatut(StatutProduit statut);

    Page<Produit> findByStatut(StatutProduit statut, Pageable pageable);

    // ═══════════════════════════════════════════════════════════
    // MÉDICAMENTS SPÉCIAUX
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT p FROM Produit p WHERE p.surOrdonnance = true AND p.statut = 'ACTIF'")
    List<Produit> findMedicamentsSurOrdonnance();

    @Query("SELECT p FROM Produit p WHERE p.estGenerique = true AND p.statut = 'ACTIF'")
    List<Produit> findGeneriques();

    @Query("SELECT p FROM Produit p WHERE p.produitReferenceId = :princepsId AND p.statut = 'ACTIF'")
    List<Produit> findGeneriquesByPrinceps(@Param("princepsId") UUID princepsId);

    @Query("SELECT p FROM Produit p WHERE p.estRemboursable = true AND p.statut = 'ACTIF'")
    List<Produit> findRemboursables();

    // ═══════════════════════════════════════════════════════════
    // CHAÎNE DU FROID
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT p FROM Produit p WHERE p.chaineFroid = true AND p.statut = 'ACTIF'")
    List<Produit> findProduitsChaineFroid();

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    @Query("SELECT COUNT(p) FROM Produit p WHERE p.statut = :statut")
    Long countByStatut(@Param("statut") StatutProduit statut);

    @Query("SELECT p.categorie, COUNT(p) FROM Produit p WHERE p.statut = 'ACTIF' GROUP BY p.categorie")
    List<Object[]> countByCategorie();
}
