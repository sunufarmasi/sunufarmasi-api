package sn.sunufarmasi.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.subscription.entity.SubscriptionPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour SubscriptionPlan
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    /**
     * Trouver un plan par son code
     */
    Optional<SubscriptionPlan> findByCode(String code);

    /**
     * Trouver tous les plans actifs
     */
    List<SubscriptionPlan> findByActifTrueOrderByOrdreAsc();

    /**
     * Vérifier si un code existe
     */
    boolean existsByCode(String code);
}