package sn.sunufarmasi.notification.enums;

/**
 * Canaux de notification
 */
public enum CanalNotification {

    APP("Application", "Notification in-app"),
    EMAIL("Email", "Notification par email"),
    SMS("SMS", "Notification par SMS"),
    WHATSAPP("WhatsApp", "Notification WhatsApp"),
    PUSH("Push", "Notification push mobile");

    private final String libelle;
    private final String description;

    CanalNotification(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public String getLibelle() { return libelle; }
    public String getDescription() { return description; }
}
