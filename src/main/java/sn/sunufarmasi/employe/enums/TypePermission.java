package sn.sunufarmasi.employe.enums;

/**
 * Types de permissions pour les employés
 *
 * Permissions granulaires pour contrôler les accès des vendeurs/employés
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum TypePermission {

    // ═══════════════════════════════════════════════════════════
    // VENTES
    // ═══════════════════════════════════════════════════════════
    VENDRE("Effectuer des ventes", "Permet d'enregistrer des ventes à la caisse"),
    ANNULER_VENTE("Annuler une vente", "Permet d'annuler une vente effectuée"),
    APPLIQUER_REMISE("Appliquer des remises", "Permet d'appliquer des remises sur les ventes"),

    // ═══════════════════════════════════════════════════════════
    // STOCK
    // ═══════════════════════════════════════════════════════════
    VOIR_STOCK("Voir le stock", "Permet de consulter le stock disponible"),
    MODIFIER_STOCK("Modifier le stock", "Permet d'ajouter/retirer du stock"),
    FAIRE_INVENTAIRE("Faire un inventaire", "Permet de réaliser un inventaire"),
    VOIR_ALERTES_STOCK("Voir les alertes stock", "Permet de voir les alertes de stock faible et péremption"),

    // ═══════════════════════════════════════════════════════════
    // COMMANDES FOURNISSEURS
    // ═══════════════════════════════════════════════════════════
    VOIR_COMMANDES("Voir les commandes", "Permet de consulter les commandes fournisseurs"),
    CREER_COMMANDE("Créer une commande", "Permet de passer une commande fournisseur"),
    VALIDER_RECEPTION("Valider une réception", "Permet de valider la réception d'une commande"),

    // ═══════════════════════════════════════════════════════════
    // CLIENTS
    // ═══════════════════════════════════════════════════════════
    VOIR_CLIENTS("Voir les clients", "Permet de consulter la liste des clients"),
    GERER_CLIENTS("Gérer les clients", "Permet d'ajouter/modifier des clients"),

    // ═══════════════════════════════════════════════════════════
    // MUTUELLES
    // ═══════════════════════════════════════════════════════════
    TRAITER_MUTUELLE("Traiter les mutuelles", "Permet de gérer les ventes avec mutuelle/tiers payant"),
    VALIDER_BON("Valider un bon", "Permet de valider les bons de prise en charge"),

    // ═══════════════════════════════════════════════════════════
    // RAPPORTS
    // ═══════════════════════════════════════════════════════════
    VOIR_RAPPORTS_BASIQUES("Voir rapports basiques", "Permet de voir les statistiques de base"),
    VOIR_RAPPORTS_AVANCES("Voir rapports avancés", "Permet de voir tous les rapports détaillés"),
    EXPORTER_DONNEES("Exporter des données", "Permet d'exporter les données en CSV/PDF"),

    // ═══════════════════════════════════════════════════════════
    // MESSAGERIE
    // ═══════════════════════════════════════════════════════════
    VOIR_MESSAGES("Voir les messages", "Permet de consulter les messages patients"),
    REPONDRE_MESSAGES("Répondre aux messages", "Permet de répondre aux messages patients");

    private final String libelle;
    private final String description;

    TypePermission(String libelle, String description) {
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
     * Permissions par défaut pour un nouveau vendeur
     */
    public static TypePermission[] getPermissionsDefaut() {
        return new TypePermission[]{
                VENDRE,
                VOIR_STOCK,
                VOIR_CLIENTS,
                VOIR_MESSAGES
        };
    }

    /**
     * Toutes les permissions (pour un employé senior/manager)
     */
    public static TypePermission[] getToutesPermissions() {
        return values();
    }
}
