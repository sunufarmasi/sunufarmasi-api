package sn.sunufarmasi.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO Response pour la connexion
 * Contient les tokens JWT et les infos utilisateur
 *
 * @author WeCan
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String userId,
        String telephone,
        String userType,
        UserInfoDto userInfo
) {
    /**
     * Informations utilisateur
     */
    public record UserInfoDto(
            String nomComplet,
            String telephone,
            String photoUrl,
            boolean telephoneVerified
    ) {}
}