package sn.sunufarmasi.syndicat.enums;

import java.math.BigDecimal;

/**
 * Plans d'abonnement pour les syndicats
 *
 * Tarification adaptée selon la portée (commune ou département)
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum PlanAbonnementSyndicat {

    TRIAL(
            "Essai Gratuit",
            BigDecimal.ZERO,
            30,  // 30 jours d'essai
            10,  // Max 10 pharmacies pendant l'essai
            false,
            false,
            "Essai gratuit d'un mois"
    ),

    STANDARD(
            "Plan Standard",
            new BigDecimal("5500"),   // 5 500 FCFA/compte/mois
            -1,  // Illimité
            20,  // Max 20 pharmacies
            true,
            false,
            "1 compte syndicat — 5 500 FCFA/mois"
    ),

    COMMUNE(
            "Plan Commune",
            new BigDecimal("5500"),   // 5 500 FCFA/compte/mois
            -1,  // Illimité
            50,  // Max 50 pharmacies (une commune)
            true,
            false,
            "Gestion des pharmacies d'une commune — 5 500 FCFA/compte"
    ),

    DEPARTEMENT(
            "Plan Département",
            new BigDecimal("8000"),   // 8 000 FCFA/mois (2 comptes inclus)
            -1,  // Illimité
            200, // Max 200 pharmacies (un département)
            true,
            true,
            "Gestion des pharmacies d'un département — 8 000 FCFA/mois"
    ),

    REGION(
            "Plan Région",
            new BigDecimal("33000"),  // 6 comptes × 5 500 FCFA
            -1,  // Illimité
            1000, // Max 1000 pharmacies (une région entière)
            true,
            true,
            "Gestion des pharmacies d'une région entière — 33 000 FCFA/mois"
    );

    private final String libelle;
    private final BigDecimal montantMensuel;
    private final int dureeJours;           // -1 = illimité (mensuel renouvelable)
    private final int nombrePharmaciesMax;
    private final boolean rapportsAvances;
    private final boolean statistiquesRegionales;
    private final String description;

    PlanAbonnementSyndicat(String libelle, BigDecimal montantMensuel, int dureeJours,
                           int nombrePharmaciesMax, boolean rapportsAvances,
                           boolean statistiquesRegionales, String description) {
        this.libelle = libelle;
        this.montantMensuel = montantMensuel;
        this.dureeJours = dureeJours;
        this.nombrePharmaciesMax = nombrePharmaciesMax;
        this.rapportsAvances = rapportsAvances;
        this.statistiquesRegionales = statistiquesRegionales;
        this.description = description;
    }

    public String getLibelle() {
        return libelle;
    }

    public BigDecimal getMontantMensuel() {
        return montantMensuel;
    }

    public int getDureeJours() {
        return dureeJours;
    }

    public int getNombrePharmaciesMax() {
        return nombrePharmaciesMax;
    }

    public boolean hasRapportsAvances() {
        return rapportsAvances;
    }

    public boolean hasStatistiquesRegionales() {
        return statistiquesRegionales;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Vérifie si c'est un plan payant
     */
    public boolean estPayant() {
        return this != TRIAL;
    }
}
