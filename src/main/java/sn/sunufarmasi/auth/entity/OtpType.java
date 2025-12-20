package sn.sunufarmasi.auth.entity;

/**
 * Types d'OTP
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum OtpType {
    /**
     * OTP pour inscription
     */
    REGISTRATION,

    /**
     * OTP pour connexion
     */
    LOGIN,

    /**
     * OTP pour réinitialisation de mot de passe
     */
    PASSWORD_RESET,

    /**
     * OTP pour rafraîchir le token JWT
     */
    TOKEN_REFRESH
}