package sn.sunufarmasi.vente.enums;

/**
 * Types de vente
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum TypeVente {

    COMPTOIR("Vente au comptoir", "Vente standard en officine"),
    ORDONNANCE("Vente sur ordonnance", "Vente avec ordonnance médicale"),
    MUTUELLE("Vente mutuelle", "Vente avec prise en charge mutuelle"),
    TIERS_PAYANT("Tiers payant", "Vente avec tiers payant"),
    LIVRAISON("Livraison", "Vente avec livraison à domicile"),
    COMMANDE("Commande client", "Commande spéciale client");

    private final String libelle;
    private final String description;

    TypeVente(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Vérifie si ce type nécessite une ordonnance
     */
    public boolean necessiteOrdonnance() {
        return this == ORDONNANCE || this == MUTUELLE || this == TIERS_PAYANT;
    }
}
