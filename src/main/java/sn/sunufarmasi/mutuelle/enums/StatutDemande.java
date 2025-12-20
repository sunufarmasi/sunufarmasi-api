package sn.sunufarmasi.mutuelle.enums;

/**
 * Statuts d'une demande de remboursement
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum StatutDemande {

    BROUILLON("Brouillon", "Demande en préparation"),
    SOUMISE("Soumise", "Demande envoyée à la mutuelle"),
    EN_TRAITEMENT("En traitement", "Demande en cours de vérification"),
    ACCEPTEE("Acceptée", "Demande acceptée, en attente de paiement"),
    PARTIELLEMENT_ACCEPTEE("Partiellement acceptée", "Acceptée avec réserves"),
    PAYEE("Payée", "Remboursement effectué"),
    REJETEE("Rejetée", "Demande rejetée"),
    ANNULEE("Annulée", "Demande annulée par la pharmacie");

    private final String libelle;
    private final String description;

    StatutDemande(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

    public boolean peutEtreModifiee() {
        return this == BROUILLON;
    }

    public boolean peutEtreSoumise() {
        return this == BROUILLON;
    }

    public boolean estTerminee() {
        return this == PAYEE || this == REJETEE || this == ANNULEE;
    }

    public boolean enAttenteReponse() {
        return this == SOUMISE || this == EN_TRAITEMENT;
    }
}
