package sn.sunufarmasi.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.notification.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant une notification
 *
 * @author WeCan
 * @since 1.0.0
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_destinataire", columnList = "destinataire_id"),
        @Index(name = "idx_notif_pharmacie", columnList = "pharmacie_id"),
        @Index(name = "idx_notif_type", columnList = "type"),
        @Index(name = "idx_notif_statut", columnList = "statut"),
        @Index(name = "idx_notif_lu", columnList = "est_lu"),
        @Index(name = "idx_notif_date", columnList = "date_creation")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ═══════════════════════════════════════════════════════════
    // DESTINATAIRE
    // ═══════════════════════════════════════════════════════════

    @Column(name = "destinataire_id")
    private UUID destinataireId;              // User ID

    @Column(name = "destinataire_nom", length = 200)
    private String destinataireNom;

    @Column(name = "destinataire_email", length = 100)
    private String destinataireEmail;

    @Column(name = "destinataire_telephone", length = 20)
    private String destinataireTelephone;

    @Column(name = "pharmacie_id")
    private UUID pharmacieId;

    // ═══════════════════════════════════════════════════════════
    // TYPE ET CANAL
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeNotification type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CanalNotification canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatutNotification statut;

    // ═══════════════════════════════════════════════════════════
    // CONTENU
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "message_court", length = 160)
    private String messageCourt;              // Pour SMS

    @Column(name = "lien_action", length = 500)
    private String lienAction;                // URL vers la page concernée

    @Column(name = "icone", length = 50)
    private String icone;                     // Nom de l'icône

    // ═══════════════════════════════════════════════════════════
    // RÉFÉRENCE
    // ═══════════════════════════════════════════════════════════

    @Column(name = "reference_type", length = 50)
    private String referenceType;             // VENTE, COMMANDE, PRODUIT, etc.

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_numero", length = 50)
    private String referenceNumero;

    // ═══════════════════════════════════════════════════════════
    // STATUT LECTURE
    // ═══════════════════════════════════════════════════════════

    @Column(name = "est_lu")
    private Boolean estLu = false;

    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;

    // ═══════════════════════════════════════════════════════════
    // ENVOI
    // ═══════════════════════════════════════════════════════════

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @Column(name = "date_livraison")
    private LocalDateTime dateLivraison;

    @Column(name = "tentatives_envoi")
    private Integer tentativesEnvoi = 0;

    @Column(name = "erreur_envoi", length = 500)
    private String erreurEnvoi;

    @Column(name = "reference_externe", length = 100)
    private String referenceExterne;          // ID SMS provider, etc.

    // ═══════════════════════════════════════════════════════════
    // PRIORITÉ
    // ═══════════════════════════════════════════════════════════

    @Column(name = "priorite")
    private Integer priorite = 5;             // 1=haute, 10=basse

    @Column(name = "expire_at")
    private LocalDateTime expireAt;           // Date d'expiration

    // ═══════════════════════════════════════════════════════════
    // MÉTADONNÉES
    // ═══════════════════════════════════════════════════════════

    @CreatedDate
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    public void marquerCommeLue() {
        this.estLu = true;
        this.dateLecture = LocalDateTime.now();
        this.statut = StatutNotification.LUE;
    }

    public void marquerCommeEnvoyee(String refExterne) {
        this.statut = StatutNotification.ENVOYEE;
        this.dateEnvoi = LocalDateTime.now();
        this.referenceExterne = refExterne;
        this.tentativesEnvoi++;
    }

    public void marquerCommeEchouee(String erreur) {
        this.statut = StatutNotification.ECHOUEE;
        this.erreurEnvoi = erreur;
        this.tentativesEnvoi++;
    }

    public boolean estExpiree() {
        return expireAt != null && LocalDateTime.now().isAfter(expireAt);
    }

    public boolean peutEtreRenvoyee() {
        return tentativesEnvoi < 3 && statut == StatutNotification.ECHOUEE;
    }

    @PrePersist
    protected void onCreate() {
        if (statut == null) statut = StatutNotification.EN_ATTENTE;
        if (estLu == null) estLu = false;
        if (tentativesEnvoi == null) tentativesEnvoi = 0;
        if (priorite == null) priorite = 5;
        if (dateCreation == null) dateCreation = LocalDateTime.now();
    }
}
