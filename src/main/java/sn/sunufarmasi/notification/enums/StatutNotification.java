package sn.sunufarmasi.notification.enums;

/**
 * Statuts d'une notification
 */
public enum StatutNotification {

    EN_ATTENTE("En attente", "Notification créée, en attente d'envoi"),
    ENVOYEE("Envoyée", "Notification envoyée"),
    LIVREE("Livrée", "Notification reçue par le destinataire"),
    LUE("Lue", "Notification lue par le destinataire"),
    ECHOUEE("Échouée", "Échec de l'envoi"),
    ANNULEE("Annulée", "Notification annulée");

    private final String libelle;
    private final String description;

    StatutNotification(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public String getLibelle() { return libelle; }
    public String getDescription() { return description; }
}
