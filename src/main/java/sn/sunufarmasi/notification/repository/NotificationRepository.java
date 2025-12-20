package sn.sunufarmasi.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.notification.entity.Notification;
import sn.sunufarmasi.notification.enums.CanalNotification;
import sn.sunufarmasi.notification.enums.StatutNotification;
import sn.sunufarmasi.notification.enums.TypeNotification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Par destinataire
    Page<Notification> findByDestinataireIdOrderByDateCreationDesc(UUID destinataireId, Pageable pageable);

    List<Notification> findByDestinataireIdAndEstLuFalseOrderByDateCreationDesc(UUID destinataireId);

    Long countByDestinataireIdAndEstLuFalse(UUID destinataireId);

    // Par pharmacie
    Page<Notification> findByPharmacieIdOrderByDateCreationDesc(UUID pharmacieId, Pageable pageable);

    List<Notification> findByPharmacieIdAndEstLuFalseOrderByPrioriteAscDateCreationDesc(UUID pharmacieId);

    // Par type
    List<Notification> findByDestinataireIdAndType(UUID destinataireId, TypeNotification type);

    // Par statut
    List<Notification> findByStatut(StatutNotification statut);

    @Query("SELECT n FROM Notification n WHERE n.statut = 'EN_ATTENTE' " +
            "AND (n.expireAt IS NULL OR n.expireAt > :now) ORDER BY n.priorite ASC")
    List<Notification> findEnAttenteEnvoi(@Param("now") LocalDateTime now);

    @Query("SELECT n FROM Notification n WHERE n.statut = 'ECHOUEE' " +
            "AND n.tentativesEnvoi < 3 ORDER BY n.priorite ASC")
    List<Notification> findAReenvoyer();

    // Marquer comme lu
    @Modifying
    @Query("UPDATE Notification n SET n.estLu = true, n.dateLecture = :now, " +
            "n.statut = 'LUE' WHERE n.id = :id")
    void marquerCommeLue(@Param("id") UUID id, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Notification n SET n.estLu = true, n.dateLecture = :now, " +
            "n.statut = 'LUE' WHERE n.destinataireId = :userId AND n.estLu = false")
    void marquerToutesCommeLues(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // Statistiques
    @Query("SELECT n.type, COUNT(n) FROM Notification n " +
            "WHERE n.pharmacieId = :pharmacieId AND n.dateCreation BETWEEN :debut AND :fin " +
            "GROUP BY n.type")
    List<Object[]> countByType(@Param("pharmacieId") UUID pharmacieId,
                                @Param("debut") LocalDateTime debut,
                                @Param("fin") LocalDateTime fin);

    // Nettoyage
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.dateCreation < :date AND n.estLu = true")
    int supprimerAnciennes(@Param("date") LocalDateTime date);
}
