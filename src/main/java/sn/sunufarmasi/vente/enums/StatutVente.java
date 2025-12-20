package sn.sunufarmasi.vente.enums;

/**
 * Statut d'une vente
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum StatutVente {

    EN_COURS("En cours", "Vente en cours de création"),
    VALIDEE("Validée", "Vente finalisée et payée"),
    PARTIELLEMENT_PAYEE("Partiellement payée", "Vente avec paiement partiel"),
    ANNULEE("Annulée", "Vente annulée"),
    REMBOURSEE("Remboursée", "Vente remboursée");

    private final String libelle;
    private final String description;

    StatutVente(String libelle, String description) {
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
        return this == EN_COURS;
    }

    public boolean peutEtreAnnulee() {
        return this == EN_COURS || this == VALIDEE || this == PARTIELLEMENT_PAYEE;
    }
}
