package sn.sunufarmasi.garde.enums;

/**
 * Statut d'une garde pharmaceutique
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum StatutGarde {

    PLANIFIEE("Planifiée", "La garde est programmée pour une date future"),
    CONFIRMEE("Confirmée", "La pharmacie a confirmé sa disponibilité"),

    EN_COURS("En cours", "La garde est actuellement active"),
    TERMINEE("Terminée", "La garde est terminée"),
    ANNULEE("Annulée", "La garde a été annulée");

    private final String libelle;
    private final String description;

    StatutGarde(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }
}
