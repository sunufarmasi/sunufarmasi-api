package sn.sunufarmasi.subscription.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.subscription.entity.Subscription;
import sn.sunufarmasi.subscription.entity.SubscriptionStatus;
import sn.sunufarmasi.subscription.repository.SubscriptionRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tâches planifiées pour la gestion des abonnements
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduledTasks {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * Vérifier les abonnements expirés (tous les jours à 2h du matin)
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void checkExpiredSubscriptions() {
        log.info("🔍 Vérification des abonnements expirés...");

        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expired = subscriptionRepository.findExpired(now);

        if (expired.isEmpty()) {
            log.info("✅ Aucun abonnement expiré");
            return;
        }

        log.info("📋 {} abonnement(s) expiré(s) trouvé(s)", expired.size());

        for (Subscription subscription : expired) {
            subscription.expire();
            subscriptionRepository.save(subscription);

            log.info("❌ Abonnement expiré: {} (Patient: {})",
                    subscription.getId(),
                    subscription.getPatient().getId());

            // TODO: Envoyer notification push au patient
            // notificationService.sendSubscriptionExpired(subscription.getPatient());
        }

        log.info("✅ Vérification terminée - {} abonnement(s) expiré(s)", expired.size());
    }

    /**
     * Notifier les abonnements qui expirent bientôt (tous les jours à 10h)
     */
    @Scheduled(cron = "0 0 10 * * *")
    @Transactional(readOnly = true)
    public void notifyExpiringSoon() {
        log.info("🔔 Notification des abonnements qui expirent bientôt...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);

        List<Subscription> expiringSoon = subscriptionRepository.findExpiringSoon(now, threeDaysLater);

        if (expiringSoon.isEmpty()) {
            log.info("✅ Aucun abonnement n'expire bientôt");
            return;
        }

        log.info("📋 {} abonnement(s) expire(nt) dans les 3 jours", expiringSoon.size());

        for (Subscription subscription : expiringSoon) {
            long daysRemaining = subscription.getDaysRemaining();

            log.info("⚠️ Abonnement {} expire dans {} jour(s) (Patient: {})",
                    subscription.getId(),
                    daysRemaining,
                    subscription.getPatient().getId());

            // TODO: Envoyer notification push
            // String message = "Votre abonnement expire dans " + daysRemaining + " jour(s). Renouvelez maintenant !";
            // notificationService.sendSubscriptionExpiring(subscription.getPatient(), message);
        }

        log.info("✅ Notifications envoyées - {} abonnement(s)", expiringSoon.size());
    }

    /**
     * Vérifier les essais gratuits expirés (tous les jours à 3h)
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void checkExpiredTrials() {
        log.info("🎁 Vérification des essais gratuits expirés...");

        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiredTrials = subscriptionRepository
                .findByIsTrialTrueAndExpiresAtBeforeAndStatus(now, SubscriptionStatus.ACTIVE);

        if (expiredTrials.isEmpty()) {
            log.info("✅ Aucun essai gratuit expiré");
            return;
        }

        log.info("📋 {} essai(s) gratuit(s) expiré(s)", expiredTrials.size());

        for (Subscription trial : expiredTrials) {
            trial.expire();
            subscriptionRepository.save(trial);

            log.info("❌ Essai gratuit expiré (Patient: {})", trial.getPatient().getId());

            // TODO: Envoyer notification pour inciter à s'abonner
            // String message = "Votre essai gratuit de 15 jours a expiré. " +
            //                  "Abonnez-vous dès maintenant pour continuer à utiliser SunuFarmasi !";
            // notificationService.sendTrialExpired(trial.getPatient(), message);
        }

        log.info("✅ Vérification terminée - {} essai(s) expiré(s)", expiredTrials.size());
    }
}