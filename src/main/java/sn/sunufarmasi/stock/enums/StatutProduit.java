package sn.sunufarmasi.stock.enums;

/**
 * Statut d'un produit dans le catalogue
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum StatutProduit {

    ACTIF("Actif", "Produit disponible à la vente"),
    INACTIF("Inactif", "Produit temporairement indisponible"),
    DISCONTINUE("Discontinué", "Produit arrêté par le fabricant"),
    EN_ATTENTE("En attente", "En attente de validation");

    private final String libelle;
    private final String description;

    StatutProduit(String libelle, String description) {
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
