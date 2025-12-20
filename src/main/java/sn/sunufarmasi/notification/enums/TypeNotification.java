package sn.sunufarmasi.notification.enums;

/**
 * Types de notifications
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum TypeNotification {

    // Stock
    RUPTURE_STOCK("Rupture de stock", "STOCK", "CRITIQUE"),
    STOCK_FAIBLE("Stock faible", "STOCK", "ATTENTION"),
    PEREMPTION_PROCHE("Péremption proche", "STOCK", "ATTENTION"),
    PRODUIT_PERIME("Produit périmé", "STOCK", "CRITIQUE"),

    // Commandes
    COMMANDE_RECUE("Commande reçue", "COMMANDE", "INFO"),
    COMMANDE_EXPEDIEE("Commande expédiée", "COMMANDE", "INFO"),
    COMMANDE_LIVREE("Commande livrée", "COMMANDE", "SUCCESS"),
    COMMANDE_ANNULEE("Commande annulée", "COMMANDE", "ATTENTION"),

    // Mutuelles
    DEMANDE_ACCEPTEE("Demande acceptée", "MUTUELLE", "SUCCESS"),
    DEMANDE_REJETEE("Demande rejetée", "MUTUELLE", "ATTENTION"),
    PAIEMENT_RECU("Paiement reçu", "MUTUELLE", "SUCCESS"),
    CREANCE_ANCIENNE("Créance ancienne", "MUTUELLE", "ATTENTION"),

    // Gardes
    RAPPEL_GARDE("Rappel garde", "GARDE", "INFO"),
    GARDE_DEMAIN("Garde demain", "GARDE", "ATTENTION"),
    GARDE_AUJOURDHUI("Garde aujourd'hui", "GARDE", "CRITIQUE"),

    // Ventes
    OBJECTIF_ATTEINT("Objectif atteint", "VENTE", "SUCCESS"),
    VENTE_IMPORTANTE("Vente importante", "VENTE", "INFO"),

    // Système
    MAINTENANCE("Maintenance prévue", "SYSTEME", "INFO"),
    MISE_A_JOUR("Mise à jour disponible", "SYSTEME", "INFO"),
    SECURITE("Alerte sécurité", "SYSTEME", "CRITIQUE"),

    // Général
    INFO_GENERALE("Information", "GENERAL", "INFO"),
    RAPPEL("Rappel", "GENERAL", "INFO");

    private final String libelle;
    private final String categorie;
    private final String niveau;

    TypeNotification(String libelle, String categorie, String niveau) {
        this.libelle = libelle;
        this.categorie = categorie;
        this.niveau = niveau;
    }

    public String getLibelle() { return libelle; }
    public String getCategorie() { return categorie; }
    public String getNiveau() { return niveau; }

    public boolean estCritique() { return "CRITIQUE".equals(niveau); }
    public boolean estAttention() { return "ATTENTION".equals(niveau); }
}
