package sn.sunufarmasi.garde.entity;

/**
 * Statuts d'une garde
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum StatutGarde {

    /**
     * Garde planifiée, en attente de confirmation
     */
    PLANIFIEE("Planifiée", "En attente de confirmation par la pharmacie", false),

    /**
     * Garde confirmée par la pharmacie
     */
    CONFIRMEE("Confirmée", "La pharmacie a confirmé sa disponibilité", false),

    /**
     * Garde en cours
     */
    EN_COURS("En cours", "La garde est actuellement en cours", true),

    /**
     * Garde terminée
     */
    TERMINEE("Terminée", "La garde est terminée", true),

    /**
     * Garde annulée
     */
    ANNULEE("Annulée", "La garde a été annulée", true),

    /**
     * Garde reportée
     */
    REPORTEE("Reportée", "La garde a été reportée à une autre date", true);

    private final String libelle;
    private final String description;
    private final boolean estFinal;

    StatutGarde(String libelle, String description, boolean estFinal) {
        this.libelle = libelle;
        this.description = description;
        this.estFinal = estFinal;
    }

    public String getLibelle() { return libelle; }
    public String getDescription() { return description; }
    public boolean isEstFinal() { return estFinal; }

    /**
     * Vérifier si la garde peut être modifiée
     */
    public boolean isModifiable() {
        return this == PLANIFIEE || this == CONFIRMEE;
    }

    /**
     * Vérifier si on peut envoyer une notification
     */
    public boolean peutNotifier() {
        return this == PLANIFIEE || this == CONFIRMEE || this == EN_COURS;
    }
}
