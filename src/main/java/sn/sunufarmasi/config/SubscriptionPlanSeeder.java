package sn.sunufarmasi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.subscription.entity.SubscriptionPlan;
import sn.sunufarmasi.subscription.repository.SubscriptionPlanRepository;

/**
 * Seed automatique des plans d'abonnement au démarrage.
 * Idempotent : ne recrée pas les plans déjà existants.
 *
 * Plans :
 *  - FREE_TRIAL  : 15 jours gratuits (essai)
 *  - MONTHLY     : 500 FCFA / mois
 *  - ANNUAL      : 5 000 FCFA / an
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanSeeder implements ApplicationRunner {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("🌱 Seed/mise à jour des plans d'abonnement...");
        upsertPlan("FREE_TRIAL", "Essai Gratuit", "15 jours d'accès à toutes les fonctionnalités premium", 0, 15, 0);
        upsertPlan("MONTHLY", "Mensuel", "Accès illimité pendant 1 mois", 500, 30, 1);
        upsertPlan("ANNUAL", "Annuel", "Accès illimité pendant 12 mois — économisez 40%", 5000, 365, 2);
        log.info("✅ Plans d'abonnement synchronisés");
    }

    private void upsertPlan(String code, String nom, String description, int prix, int dureeJours, int ordre) {
        subscriptionPlanRepository.findByCode(code).ifPresentOrElse(
            existing -> {
                existing.setNom(nom);
                existing.setDescription(description);
                existing.setPrix(prix);
                existing.setDureeJours(dureeJours);
                existing.setOrdre(ordre);
                existing.setActif(true);
                subscriptionPlanRepository.save(existing);
                log.debug("↻ Plan mis à jour: {} ({}  FCFA)", code, prix);
            },
            () -> {
                subscriptionPlanRepository.save(SubscriptionPlan.builder()
                        .code(code).nom(nom).description(description)
                        .prix(prix).dureeJours(dureeJours).actif(true).ordre(ordre)
                        .build());
                log.debug("+ Plan créé: {} ({} FCFA)", code, prix);
            }
        );
    }
}
