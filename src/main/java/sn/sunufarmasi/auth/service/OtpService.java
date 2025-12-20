package sn.sunufarmasi.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.auth.entity.OtpCode;
import sn.sunufarmasi.auth.entity.OtpType;
import sn.sunufarmasi.auth.repository.OtpRepository;
import sn.sunufarmasi.shared.exception.BadRequestException;
import sn.sunufarmasi.shared.exception.TooManyRequestsException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service pour la gestion des codes OTP
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OtpService {

    private final OtpRepository otpRepository;
    private final SmsService smsService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_PER_HOUR = 5;
    private static final int MAX_ATTEMPTS = 3;

    // ═══════════════════════════════════════════════════════════
    // GÉNÉRATION OTP
    // ═══════════════════════════════════════════════════════════

    /**
     * Générer et envoyer un code OTP
     *
     * @param telephone Numéro de téléphone
     * @param type Type d'OTP
     * @return Code OTP créé
     */
    @Transactional
    public OtpCode generateAndSendOtp(String telephone, OtpType type) {
        log.info("🔐 Génération OTP pour {} - Type: {}", telephone, type);

        // Vérifier le rate limiting (max 5 OTP par heure)
        checkRateLimit(telephone);

        // Invalider les anciens OTP du même type
        otpRepository.deleteByTelephoneAndType(telephone, type);

        // Générer le code OTP
        String code = generateOtpCode();

        // Créer l'entité OTP
        OtpCode otpCode = OtpCode.builder()
                .telephone(telephone)
                .code(code)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        otpCode = otpRepository.save(otpCode);

        // Envoyer le SMS
        sendOtpSms(telephone, code, type);

        log.info("✅ OTP généré et envoyé: {}", otpCode.getId());

        return otpCode;
    }

    /**
     * Générer un code OTP aléatoire de 6 chiffres
     */
    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 100000 à 999999
        return String.valueOf(code);
    }

    // ═══════════════════════════════════════════════════════════
    // VÉRIFICATION OTP
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifier un code OTP
     *
     * @param telephone Numéro de téléphone
     * @param code Code OTP fourni
     * @param type Type d'OTP
     * @return OtpCode vérifié
     */
    @Transactional
    public OtpCode verifyOtp(String telephone, String code, OtpType type) {
        log.info("🔍 Vérification OTP pour {} - Type: {}", telephone, type);

        // Trouver l'OTP
        OtpCode otpCode = otpRepository
                .findByTelephoneAndCodeAndType(telephone, code, type)
                .orElseThrow(() -> new BadRequestException("Code OTP invalide"));

        // Vérifier si déjà vérifié
        if (otpCode.isVerified()) {
            throw new BadRequestException("Ce code OTP a déjà été utilisé");
        }

        // Vérifier si expiré
        if (otpCode.isExpired()) {
            throw new BadRequestException("Ce code OTP a expiré. Demandez un nouveau code.");
        }

        // Vérifier le nombre de tentatives
        if (otpCode.isMaxAttemptsReached()) {
            throw new BadRequestException(
                    "Nombre maximum de tentatives atteint. Demandez un nouveau code."
            );
        }

        // Incrémenter les tentatives
        otpCode.incrementAttempts();

        // Marquer comme vérifié
        otpCode.markAsVerified();

        otpRepository.save(otpCode);

        log.info("✅ OTP vérifié avec succès: {}", otpCode.getId());

        return otpCode;
    }

    /**
     * Vérifier si un OTP valide existe pour un téléphone et un type
     */
    public boolean hasValidOtp(String telephone, OtpType type) {
        Optional<OtpCode> otp = otpRepository.findLatestValidOtp(
                telephone, type, LocalDateTime.now()
        );
        return otp.isPresent() && otp.get().canBeUsed();
    }

    // ═══════════════════════════════════════════════════════════
    // ENVOI SMS
    // ═══════════════════════════════════════════════════════════

    /**
     * Envoyer le SMS OTP
     */
    private void sendOtpSms(String telephone, String code, OtpType type) {
        String message = buildOtpMessage(code, type);

        try {
            smsService.sendSms(telephone, message);
            log.info("📱 SMS OTP envoyé à {}", telephone);
        } catch (Exception e) {
            log.error("❌ Erreur envoi SMS OTP: {}", e.getMessage());
            // Ne pas bloquer le processus si l'envoi SMS échoue
            // En dev, on peut simplement logger le code
            log.warn("⚠️ MODE DEV - Code OTP: {}", code);
        }
    }

    /**
     * Construire le message SMS selon le type d'OTP
     */
    private String buildOtpMessage(String code, OtpType type) {
        return switch (type) {
            case REGISTRATION -> String.format(
                    "SunuFarmasi: Votre code de vérification est %s. Valide 5 minutes.",
                    code
            );
            case LOGIN -> String.format(
                    "SunuFarmasi: Votre code de connexion est %s. Valide 5 minutes.",
                    code
            );
            case PASSWORD_RESET -> String.format(
                    "SunuFarmasi: Votre code de réinitialisation est %s. Valide 5 minutes.",
                    code
            );
            case TOKEN_REFRESH -> String.format(
                    "SunuFarmasi: Votre code de vérification est %s. Valide 5 minutes.",
                    code
            );
        };
    }

    // ═══════════════════════════════════════════════════════════
    // RATE LIMITING
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifier le rate limiting (anti-spam)
     */
    private void checkRateLimit(String telephone) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentOtpCount = otpRepository.countRecentOtps(telephone, oneHourAgo);

        if (recentOtpCount >= MAX_OTP_PER_HOUR) {
            log.warn("⚠️ Rate limit dépassé pour {}: {} OTP en 1h", telephone, recentOtpCount);
            throw new TooManyRequestsException(
                    "Trop de demandes de code. Veuillez réessayer dans 1 heure."
            );
        }
    }

    // ═══════════════════════════════════════════════════════════
    // NETTOYAGE AUTOMATIQUE
    // ═══════════════════════════════════════════════════════════

    /**
     * Nettoyer les OTP expirés (toutes les heures)
     */
    @Scheduled(cron = "0 0 * * * *") // Toutes les heures
    @Transactional
    public void cleanupExpiredOtps() {
        log.info("🧹 Nettoyage des OTP expirés...");

        LocalDateTime now = LocalDateTime.now();
        otpRepository.deleteExpiredOtps(now);

        log.info("✅ Nettoyage terminé");
    }
}