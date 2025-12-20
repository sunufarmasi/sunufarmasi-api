package sn.sunufarmasi.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.notification.enums.CanalNotification;
import sn.sunufarmasi.notification.enums.TypeNotification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Configuration des préférences de notification par utilisateur
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "config_notifications", indexes = {
        @Index(name = "idx_config_user", columnList = "user_id"),
        @Index(name = "idx_config_pharmacie", columnList = "pharmacie_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ConfigNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "pharmacie_id")
    private UUID pharmacieId;

    // ═══════════════════════════════════════════════════════════
    // CANAUX ACTIVÉS
    // ═══════════════════════════════════════════════════════════

    @Column(name = "app_active")
    private Boolean appActive = true;

    @Column(name = "email_active")
    private Boolean emailActive = true;

    @Column(name = "sms_active")
    private Boolean smsActive = false;

    @Column(name = "whatsapp_active")
    private Boolean whatsappActive = false;

    @Column(name = "push_active")
    private Boolean pushActive = true;

    // ═══════════════════════════════════════════════════════════
    // TYPES DE NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════

    @Column(name = "notif_stock")
    private Boolean notifStock = true;

    @Column(name = "notif_commande")
    private Boolean notifCommande = true;

    @Column(name = "notif_mutuelle")
    private Boolean notifMutuelle = true;

    @Column(name = "notif_garde")
    private Boolean notifGarde = true;

    @Column(name = "notif_vente")
    private Boolean notifVente = false;

    @Column(name = "notif_systeme")
    private Boolean notifSysteme = true;

    // ═══════════════════════════════════════════════════════════
    // PLAGES HORAIRES (NE PAS DÉRANGER)
    // ═══════════════════════════════════════════════════════════

    @Column(name = "heure_debut_silence")
    private LocalTime heureDebutSilence;      // Ex: 22:00

    @Column(name = "heure_fin_silence")
    private LocalTime heureFinSilence;        // Ex: 07:00

    @Column(name = "silence_weekend")
    private Boolean silenceWeekend = false;

    // ═══════════════════════════════════════════════════════════
    // FRÉQUENCE RÉSUMÉ
    // ═══════════════════════════════════════════════════════════

    @Column(name = "resume_quotidien")
    private Boolean resumeQuotidien = false;

    @Column(name = "heure_resume")
    private LocalTime heureResume;            // Ex: 08:00

    @Column(name = "resume_hebdomadaire")
    private Boolean resumeHebdomadaire = true;

    @Column(name = "jour_resume")
    private Integer jourResume = 1;           // 1=Lundi

    // ═══════════════════════════════════════════════════════════
    // SEUILS ALERTES STOCK
    // ═══════════════════════════════════════════════════════════

    @Column(name = "seuil_rupture_email")
    private Boolean seuilRuptureEmail = true;

    @Column(name = "seuil_rupture_sms")
    private Boolean seuilRuptureSms = true;

    @Column(name = "jours_avant_peremption")
    private Integer joursAvantPeremption = 90;

    // ═══════════════════════════════════════════════════════════
    // MÉTADONNÉES
    // ═══════════════════════════════════════════════════════════

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    public boolean estEnModeSilence() {
        if (heureDebutSilence == null || heureFinSilence == null) return false;

        LocalTime now = LocalTime.now();

        if (heureDebutSilence.isBefore(heureFinSilence)) {
            return now.isAfter(heureDebutSilence) && now.isBefore(heureFinSilence);
        } else {
            return now.isAfter(heureDebutSilence) || now.isBefore(heureFinSilence);
        }
    }

    public boolean accepteCanal(CanalNotification canal) {
        return switch (canal) {
            case APP -> appActive != null && appActive;
            case EMAIL -> emailActive != null && emailActive;
            case SMS -> smsActive != null && smsActive;
            case WHATSAPP -> whatsappActive != null && whatsappActive;
            case PUSH -> pushActive != null && pushActive;
        };
    }

    public boolean accepteType(TypeNotification type) {
        String categorie = type.getCategorie();
        return switch (categorie) {
            case "STOCK" -> notifStock != null && notifStock;
            case "COMMANDE" -> notifCommande != null && notifCommande;
            case "MUTUELLE" -> notifMutuelle != null && notifMutuelle;
            case "GARDE" -> notifGarde != null && notifGarde;
            case "VENTE" -> notifVente != null && notifVente;
            case "SYSTEME" -> notifSysteme != null && notifSysteme;
            default -> true;
        };
    }

    @PrePersist
    protected void onCreate() {
        if (appActive == null) appActive = true;
        if (emailActive == null) emailActive = true;
        if (smsActive == null) smsActive = false;
        if (pushActive == null) pushActive = true;
        if (notifStock == null) notifStock = true;
        if (notifCommande == null) notifCommande = true;
        if (notifMutuelle == null) notifMutuelle = true;
        if (notifGarde == null) notifGarde = true;
        if (notifVente == null) notifVente = false;
        if (notifSysteme == null) notifSysteme = true;
    }
}
