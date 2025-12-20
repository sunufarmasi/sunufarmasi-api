package sn.sunufarmasi.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.auth.entity.VerificationOtp;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité VerificationOtp
 * 
 * @author WeCan
 * @since 1.0.0
 */
@Repository
public interface VerificationOtpRepository extends JpaRepository<sn.sunufarmasi.auth.entity.VerificationOtp, UUID> {
    
    /**
     * Trouver le dernier OTP non vérifié pour un email
     * 
     * @param email Email de l'utilisateur
     * @return Optional<VerificationOtp>
     */
    @Query("SELECT v FROM VerificationOtp v WHERE v.email = :email AND v.verifie = false " +
           "ORDER BY v.createdAt DESC LIMIT 1")
    Optional<VerificationOtp> findLatestUnverifiedByEmail(@Param("email") String email);
    
    /**
     * Trouver le dernier OTP non vérifié pour un téléphone
     * 
     * @param telephone Numéro de téléphone
     * @return Optional<VerificationOtp>
     */
    @Query("SELECT v FROM VerificationOtp v WHERE v.telephone = :telephone AND v.verifie = false " +
           "ORDER BY v.createdAt DESC LIMIT 1")
    Optional<VerificationOtp> findLatestUnverifiedByTelephone(@Param("telephone") String telephone);
    
    /**
     * Trouver un OTP par email, code et non vérifié
     * 
     * @param email Email
     * @param codeOtp Code OTP
     * @return Optional<VerificationOtp>
     */
    Optional<VerificationOtp> findByEmailAndCodeOtpAndVerifieFalse(String email, String codeOtp);
    
    /**
     * Trouver un OTP par téléphone, code et non vérifié
     * 
     * @param telephone Téléphone
     * @param codeOtp Code OTP
     * @return Optional<VerificationOtp>
     */
    Optional<VerificationOtp> findByTelephoneAndCodeOtpAndVerifieFalse(String telephone, String codeOtp);
    
    /**
     * Vérifier si un email a un OTP vérifié
     * 
     * @param email Email
     * @return true si existe
     */
    boolean existsByEmailAndVerifieTrue(String email);
    
    /**
     * Vérifier si un téléphone a un OTP vérifié
     * 
     * @param telephone Téléphone
     * @return true si existe
     */
    boolean existsByTelephoneAndVerifieTrue(String telephone);
    
    /**
     * Compter les OTPs non vérifiés créés récemment pour un email
     * (Pour éviter le spam)
     * 
     * @param email Email
     * @param since Date depuis laquelle compter
     * @return Nombre d'OTPs
     */
    @Query("SELECT COUNT(v) FROM VerificationOtp v WHERE v.email = :email " +
           "AND v.verifie = false AND v.createdAt > :since")
    long countRecentUnverifiedByEmail(@Param("email") String email, @Param("since") LocalDateTime since);
    
    /**
     * Supprimer les OTPs expirés (nettoyage automatique)
     * 
     * @param now Date actuelle
     */
    @Modifying
    @Query("DELETE FROM VerificationOtp v WHERE v.expirationDate < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
    
    /**
     * Supprimer les OTPs vérifiés de plus de 24h (nettoyage)
     * 
     * @param before Date limite
     */
    @Modifying
    @Query("DELETE FROM VerificationOtp v WHERE v.verifie = true AND v.verifiedAt < :before")
    void deleteOldVerified(@Param("before") LocalDateTime before);
    
    /**
     * Supprimer tous les OTPs non vérifiés d'un email
     * 
     * @param email Email
     */
    @Modifying
    @Query("DELETE FROM VerificationOtp v WHERE v.email = :email AND v.verifie = false")
    void deleteUnverifiedByEmail(@Param("email") String email);
}
