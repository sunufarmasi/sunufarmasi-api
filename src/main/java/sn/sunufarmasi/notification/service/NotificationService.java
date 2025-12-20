package sn.sunufarmasi.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.notification.dto.*;
import sn.sunufarmasi.notification.entity.*;
import sn.sunufarmasi.notification.enums.*;
import sn.sunufarmasi.notification.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ConfigNotificationRepository configRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    // ═══════════════════════════════════════════════════════════
    // CRÉATION NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════

    public NotificationResponse creer(UUID pharmacieId, CreateNotificationRequest req) {
        log.info("Création notification: {} - {}", req.type(), req.titre());

        Notification n = Notification.builder()
                .pharmacieId(pharmacieId)
                .destinataireId(req.destinataireId())
                .destinataireEmail(req.destinataireEmail())
                .destinataireTelephone(req.destinataireTelephone())
                .type(req.type())
                .canal(req.canal())
                .statut(StatutNotification.EN_ATTENTE)
                .titre(req.titre())
                .message(req.message())
                .messageCourt(req.messageCourt())
                .lienAction(req.lienAction())
                .icone(req.icone())
                .referenceType(req.referenceType())
                .referenceId(req.referenceId())
                .referenceNumero(req.referenceNumero())
                .priorite(req.priorite() != null ? req.priorite() : 5)
                .build();

        n = notificationRepository.save(n);

        // Envoi asynchrone
        envoyerAsync(n);

        return toResponse(n);
    }

    /**
     * Créer notification rapide in-app
     */
    public void notifierApp(UUID pharmacieId, UUID userId, TypeNotification type,
                            String titre, String message, String lien) {
        Notification n = Notification.builder()
                .pharmacieId(pharmacieId)
                .destinataireId(userId)
                .type(type)
                .canal(CanalNotification.APP)
                .statut(StatutNotification.ENVOYEE)
                .titre(titre)
                .message(message)
                .lienAction(lien)
                .priorite(type.estCritique() ? 1 : (type.estAttention() ? 3 : 5))
                .dateEnvoi(LocalDateTime.now())
                .build();

        notificationRepository.save(n);
    }

    /**
     * Notifier tous les utilisateurs d'une pharmacie
     */
    public void notifierPharmacie(UUID pharmacieId, TypeNotification type,
                                   String titre, String message, String lien) {
        log.info("Notification pharmacie {}: {}", pharmacieId, titre);

        // TODO: Récupérer tous les users de la pharmacie
        // Pour l'instant, créer une notification globale
        Notification n = Notification.builder()
                .pharmacieId(pharmacieId)
                .type(type)
                .canal(CanalNotification.APP)
                .statut(StatutNotification.ENVOYEE)
                .titre(titre)
                .message(message)
                .lienAction(lien)
                .priorite(type.estCritique() ? 1 : 5)
                .dateEnvoi(LocalDateTime.now())
                .build();

        notificationRepository.save(n);
    }

    // ═══════════════════════════════════════════════════════════
    // ALERTES AUTOMATIQUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Alerte rupture de stock
     */
    public void alerterRuptureStock(UUID pharmacieId, String produitNom, String produitCode) {
        notifierPharmacie(pharmacieId, TypeNotification.RUPTURE_STOCK,
                "Rupture de stock: " + produitNom,
                "Le produit " + produitNom + " (" + produitCode + ") est en rupture de stock.",
                "/stock/ruptures"
        );
    }

    /**
     * Alerte stock faible
     */
    public void alerterStockFaible(UUID pharmacieId, String produitNom, int stockActuel, int seuil) {
        notifierPharmacie(pharmacieId, TypeNotification.STOCK_FAIBLE,
                "Stock faible: " + produitNom,
                "Le stock de " + produitNom + " est à " + stockActuel + " unités (seuil: " + seuil + ").",
                "/stock/alertes"
        );
    }

    /**
     * Alerte péremption proche
     */
    public void alerterPeremption(UUID pharmacieId, String produitNom, String lot, int joursRestants) {
        notifierPharmacie(pharmacieId, TypeNotification.PEREMPTION_PROCHE,
                "Péremption proche: " + produitNom,
                "Le lot " + lot + " de " + produitNom + " expire dans " + joursRestants + " jours.",
                "/stock/peremptions"
        );
    }

    /**
     * Rappel garde
     */
    public void rappelerGarde(UUID pharmacieId, UUID userId, LocalDate dateGarde) {
        notifierApp(pharmacieId, userId, TypeNotification.RAPPEL_GARDE,
                "Rappel: Garde programmée",
                "Vous êtes de garde le " + dateGarde + ".",
                "/gardes"
        );
    }

    // ═══════════════════════════════════════════════════════════
    // ENVOI
    // ═══════════════════════════════════════════════════════════

    @Async
    public void envoyerAsync(Notification n) {
        try {
            switch (n.getCanal()) {
                case EMAIL -> envoyerEmail(n);
                case SMS -> envoyerSms(n);
                case APP, PUSH -> n.marquerCommeEnvoyee(null);
                default -> log.warn("Canal non supporté: {}", n.getCanal());
            }
            notificationRepository.save(n);
        } catch (Exception e) {
            log.error("Erreur envoi notification {}: {}", n.getId(), e.getMessage());
            n.marquerCommeEchouee(e.getMessage());
            notificationRepository.save(n);
        }
    }

    private void envoyerEmail(Notification n) {
//        if (n.getDestinataireEmail() == null) {
//            throw new IllegalStateException("Email destinataire manquant");
//        }
//        String ref = emailService.envoyer(n.getDestinataireEmail(), n.getTitre(), n.getMessage());
//        n.marquerCommeEnvoyee(ref);
    }

    private void envoyerSms(Notification n) {
        if (n.getDestinataireTelephone() == null) {
            throw new IllegalStateException("Téléphone destinataire manquant");
        }
        String msg = n.getMessageCourt() != null ? n.getMessageCourt() : n.getTitre();
        String ref = smsService.envoyer(n.getDestinataireTelephone(), msg);
        n.marquerCommeEnvoyee(ref);
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURE
    // ═══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getByUser(UUID userId, Pageable pageable) {
        return notificationRepository.findByDestinataireIdOrderByDateCreationDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNonLues(UUID userId) {
        return notificationRepository.findByDestinataireIdAndEstLuFalseOrderByDateCreationDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long countNonLues(UUID userId) {
        return notificationRepository.countByDestinataireIdAndEstLuFalse(userId);
    }

    public void marquerCommeLue(UUID notifId) {
        notificationRepository.marquerCommeLue(notifId, LocalDateTime.now());
    }

    public void marquerToutesCommeLues(UUID userId) {
        notificationRepository.marquerToutesCommeLues(userId, LocalDateTime.now());
    }

    // ═══════════════════════════════════════════════════════════
    // CONFIGURATION
    // ═══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public ConfigNotificationDTO getConfig(UUID userId) {
        ConfigNotification config = configRepository.findByUserId(userId)
                .orElse(ConfigNotification.builder().userId(userId).build());
        return toConfigDTO(config);
    }

    public ConfigNotificationDTO updateConfig(UUID userId, ConfigNotificationDTO dto) {
        ConfigNotification config = configRepository.findByUserId(userId)
                .orElse(ConfigNotification.builder().userId(userId).build());

        config.setAppActive(dto.appActive());
        config.setEmailActive(dto.emailActive());
        config.setSmsActive(dto.smsActive());
        config.setWhatsappActive(dto.whatsappActive());
        config.setPushActive(dto.pushActive());
        config.setNotifStock(dto.notifStock());
        config.setNotifCommande(dto.notifCommande());
        config.setNotifMutuelle(dto.notifMutuelle());
        config.setNotifGarde(dto.notifGarde());
        config.setNotifVente(dto.notifVente());
        config.setNotifSysteme(dto.notifSysteme());
        config.setHeureDebutSilence(dto.heureDebutSilence());
        config.setHeureFinSilence(dto.heureFinSilence());
        config.setSilenceWeekend(dto.silenceWeekend());
        config.setResumeQuotidien(dto.resumeQuotidien());
        config.setHeureResume(dto.heureResume());
        config.setResumeHebdomadaire(dto.resumeHebdomadaire());
        config.setJourResume(dto.jourResume());
        config.setSeuilRuptureEmail(dto.seuilRuptureEmail());
        config.setSeuilRuptureSms(dto.seuilRuptureSms());
        config.setJoursAvantPeremption(dto.joursAvantPeremption());

        return toConfigDTO(configRepository.save(config));
    }

    // ═══════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType(), n.getType().getLibelle(),
                n.getType().getCategorie(), n.getType().getNiveau(),
                n.getCanal(), n.getStatut(),
                n.getTitre(), n.getMessage(), n.getLienAction(), n.getIcone(),
                n.getReferenceType(), n.getReferenceId(), n.getReferenceNumero(),
                n.getEstLu(), n.getDateLecture(), n.getDateCreation(), n.getDateEnvoi(),
                n.getPriorite()
        );
    }

    private ConfigNotificationDTO toConfigDTO(ConfigNotification c) {
        return new ConfigNotificationDTO(
                c.getId(), c.getUserId(),
                c.getAppActive(), c.getEmailActive(), c.getSmsActive(),
                c.getWhatsappActive(), c.getPushActive(),
                c.getNotifStock(), c.getNotifCommande(), c.getNotifMutuelle(),
                c.getNotifGarde(), c.getNotifVente(), c.getNotifSysteme(),
                c.getHeureDebutSilence(), c.getHeureFinSilence(), c.getSilenceWeekend(),
                c.getResumeQuotidien(), c.getHeureResume(),
                c.getResumeHebdomadaire(), c.getJourResume(),
                c.getSeuilRuptureEmail(), c.getSeuilRuptureSms(), c.getJoursAvantPeremption()
        );
    }
}
