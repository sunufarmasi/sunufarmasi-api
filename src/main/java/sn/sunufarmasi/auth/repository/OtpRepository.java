package sn.sunufarmasi.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.auth.entity.OtpCode;
import sn.sunufarmasi.auth.entity.OtpType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité OtpCode
 *
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface OtpRepository extends JpaRepository<OtpCode, UUID> {

    /**
     * Trouver le dernier OTP valide pour un téléphone et un type
     */
    @Query("""
        SELECT o FROM OtpCode o 
        WHERE o.telephone = :telephone 
        AND o.type = :type 
        AND o.verified = false 
        AND o.expiresAt > :now
        ORDER BY o.createdAt DESC
        LIMIT 1
        """)
    Optional<OtpCode> findLatestValidOtp(
            @Param("telephone") String telephone,
            @Param("type") OtpType type,
            @Param("now") LocalDateTime now
    );

    /**
     * Trouver un OTP par téléphone, code et type
     */
    Optional<OtpCode> findByTelephoneAndCodeAndType(
            String telephone,
            String code,
            OtpType type
    );

    /**
     * Supprimer tous les OTP expirés
     */
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);

    /**
     * Supprimer tous les OTP d'un téléphone
     */
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.telephone = :telephone")
    void deleteByTelephone(@Param("telephone") String telephone);

    /**
     * Supprimer tous les OTP d'un téléphone et type
     */
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.telephone = :telephone AND o.type = :type")
    void deleteByTelephoneAndType(
            @Param("telephone") String telephone,
            @Param("type") OtpType type
    );

    /**
     * Compter les OTP créés dans les N dernières minutes pour un téléphone
     * (Protection anti-spam)
     */
    @Query("""
        SELECT COUNT(o) FROM OtpCode o 
        WHERE o.telephone = :telephone 
        AND o.createdAt > :since
        """)
    long countRecentOtps(
            @Param("telephone") String telephone,
            @Param("since") LocalDateTime since
    );
}