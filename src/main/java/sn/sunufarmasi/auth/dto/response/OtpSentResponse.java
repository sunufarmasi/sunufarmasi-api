package sn.sunufarmasi.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DTO Response après envoi d'un OTP
 *
 * @author WeCan
 * @since 1.0.0
 */
public record OtpSentResponse(
        String message,
        String telephone,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime expiresAt,

        int expiresInMinutes
) {
    /**
     * Constructeur avec message par défaut
     */
    public OtpSentResponse(String telephone, LocalDateTime expiresAt, int expiresInMinutes) {
        this(
                "Code de vérification envoyé par SMS",
                telephone,
                expiresAt,
                expiresInMinutes
        );
    }
}