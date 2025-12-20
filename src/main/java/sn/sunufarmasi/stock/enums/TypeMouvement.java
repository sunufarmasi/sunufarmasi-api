package sn.sunufarmasi.stock.enums;

/**
 * Types de mouvements de stock
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum TypeMouvement {

    // ═══════════════════════════════════════════════════════════
    // ENTRÉES (augmentent le stock)
    // ═══════════════════════════════════════════════════════════
    ACHAT("Achat fournisseur", true, "Réception de commande fournisseur"),
    RETOUR_CLIENT("Retour client", true, "Retour de produit par un client"),
    TRANSFERT_ENTRANT("Transfert entrant", true, "Transfert depuis une autre pharmacie"),
    AJUSTEMENT_POSITIF("Ajustement positif", true, "Correction d'inventaire en hausse"),
    DON_RECU("Don reçu", true, "Don reçu"),
    INITIALISATION("Initialisation", true, "Stock initial"),

    // ═══════════════════════════════════════════════════════════
    // SORTIES (diminuent le stock)
    // ═══════════════════════════════════════════════════════════
    VENTE("Vente", false, "Vente à un client"),
    RETOUR_FOURNISSEUR("Retour fournisseur", false, "Retour au fournisseur"),
    TRANSFERT_SORTANT("Transfert sortant", false, "Transfert vers une autre pharmacie"),
    AJUSTEMENT_NEGATIF("Ajustement négatif", false, "Correction d'inventaire en baisse"),
    PEREMPTION("Péremption", false, "Produit périmé détruit"),
    CASSE("Casse", false, "Produit cassé/endommagé"),
    VOL("Vol", false, "Produit volé"),
    DON_EMIS("Don émis", false, "Don effectué"),
    ECHANTILLON("Échantillon", false, "Distribution d'échantillon");

    private final String libelle;
    private final boolean entree;
    private final String description;

    TypeMouvement(String libelle, boolean entree, String description) {
        this.libelle = libelle;
        this.entree = entree;
        this.description = description;
    }

    public String getLibelle() {
        return libelle;
    }

    public boolean isEntree() {
        return entree;
    }

    public boolean isSortie() {
        return !entree;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Calcule l'impact sur le stock (+quantité ou -quantité)
     */
    public int calculerImpact(int quantite) {
        return entree ? quantite : -quantite;
    }
}
