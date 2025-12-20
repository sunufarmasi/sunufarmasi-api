package sn.sunufarmasi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO Request pour rafraîchir un token JWT
 * 
 * @author WeCan
 * @since 1.0.0
 */
public record RefreshTokenRequest(
    @NotBlank(message = "Le refresh token est obligatoire")
    String refreshToken
) {}
