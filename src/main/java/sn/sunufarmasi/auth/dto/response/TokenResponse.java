package sn.sunufarmasi.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO Response contenant les tokens JWT
 *
 * @author WeCan
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,  // Secondes

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime expiresAt,

        UserInfoDto user
) {
    /**
     * DTO pour les infos utilisateur
     */
    public record UserInfoDto(
            String id,
            String telephone,
            String email,
            String nomComplet,
            boolean emailVerified,
            boolean telephoneVerified
    ) {}

    /**
     * Constructeur par défaut avec tokenType = "Bearer"
     */
    public TokenResponse(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            LocalDateTime expiresAt,
            UserInfoDto user
    ) {
        this(accessToken, refreshToken, "Bearer", expiresIn, expiresAt, user);
    }
}