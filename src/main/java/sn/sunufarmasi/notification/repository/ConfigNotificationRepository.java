package sn.sunufarmasi.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.notification.entity.ConfigNotification;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfigNotificationRepository extends JpaRepository<ConfigNotification, UUID> {

    Optional<ConfigNotification> findByUserId(UUID userId);

    Optional<ConfigNotification> findByUserIdAndPharmacieId(UUID userId, UUID pharmacieId);

    boolean existsByUserId(UUID userId);
}
