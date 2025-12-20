package sn.sunufarmasi.pharmacie.enums;

import java.math.BigDecimal;

/**
 * Plans d'abonnement pour les pharmacies
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum PlanAbonnementPharmacie {
    /**
     * Essai gratuit - 1 mois
     * - Toutes les fonctionnalités de base
     * - Activation automatique
     */
    TRIAL(BigDecimal.ZERO, 1, 1, "Essai gratuit 1 mois"),

    /**
     * Plan BASIC - 10 000 FCFA/mois
     * - 1 pharmacie
     * - 1 employé
     * - Stock illimité
     * - Ventes
     * - Rapports basiques
     */
    BASIC(new BigDecimal("10000"), 1, 1, "Plan Basic"),

    /**
     * Plan PREMIUM - 25 000 FCFA/mois
     * - 1 pharmacie
     * - 5 employés
     * - Toutes fonctionnalités BASIC
     * - Messagerie
     * - Rapports avancés
     * - Support prioritaire
     */
    PREMIUM(new BigDecimal("25000"), 5, 1, "Plan Premium"),

    /**
     * Plan ENTERPRISE - 50 000 FCFA/mois
     * - Pharmacies illimitées
     * - Employés illimités
     * - Toutes fonctionnalités PREMIUM
     * - API access
     * - Support 24/7
     * - Multi-pharmacies
     */
    ENTERPRISE(new BigDecimal("50000"), Integer.MAX_VALUE, Integer.MAX_VALUE, "Plan Enterprise");

    private final BigDecimal montantMensuel;
    private final Integer nombreEmployesMax;
    private final Integer nombrePharmaciesMax;
    private final String libelle;

    PlanAbonnementPharmacie(BigDecimal montantMensuel, Integer nombreEmployesMax, Integer nombrePharmaciesMax, String libelle) {
        this.montantMensuel = montantMensuel;
        this.nombreEmployesMax = nombreEmployesMax;
        this.nombrePharmaciesMax = nombrePharmaciesMax;
        this.libelle = libelle;
    }

    public BigDecimal getMontantMensuel() {
        return montantMensuel;
    }

    public Integer getNombreEmployesMax() {
        return nombreEmployesMax;
    }

    public Integer getNombrePharmaciesMax() {
        return nombrePharmaciesMax;
    }

    public String getLibelle() {
        return libelle;
    }

    /**
     * Vérifie si le plan permet un nombre illimité d'employés
     */
    public boolean employesIllimites() {
        return this == ENTERPRISE;
    }

    /**
     * Vérifie si le plan permet plusieurs pharmacies
     */
    public boolean multiPharmacies() {
        return this == ENTERPRISE;
    }

    /**
     * Vérifie si c'est un plan payant
     */
    public boolean estPayant() {
        return this != TRIAL;
    }
}