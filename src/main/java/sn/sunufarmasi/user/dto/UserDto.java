package sn.sunufarmasi.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import sn.sunufarmasi.user.entity.User.RoleUser;
import sn.sunufarmasi.user.entity.User.StatutUser;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTOs pour l'authentification User (email/password)
 * Distinct du système OTP des patients
 */
public class UserDto {

    // ═══════════════════════════════════════════════════════════════════════════
    // REQUESTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Login avec email/password (Pharmaciens, Syndicats, Admins)
     */
    public record LoginRequest(
            @NotBlank(message = "L'email est obligatoire")
            @Email(message = "Format email invalide")
            String email,

            @NotBlank(message = "Le mot de passe est obligatoire")
            String password
    ) {}

    /**
     * Inscription avec email/password
     */
    public record RegisterRequest(
            @NotBlank(message = "L'email est obligatoire")
            @Email(message = "Format email invalide")
            String email,

            @NotBlank(message = "Le mot de passe est obligatoire")
            @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
            String password,

            @NotBlank(message = "Le nom est obligatoire")
            String nom,

            @NotBlank(message = "Le prénom est obligatoire")
            String prenom,

            String telephone,

            RoleUser role
    ) {}

    /**
     * Refresh token
     */
    public record RefreshTokenRequest(
            @NotBlank(message = "Le refresh token est obligatoire")
            String refreshToken
    ) {}

    /**
     * Modifier le profil
     */
    public record UpdateProfileRequest(
            @Size(max = 50) String nom,
            @Size(max = 50) String prenom,
            @Size(max = 20) String telephone,
            @Size(max = 200) String adresse,
            String avatar
    ) {}

    /**
     * Changer le mot de passe
     */
    public record ChangePasswordRequest(
            @NotBlank(message = "L'ancien mot de passe est obligatoire")
            String ancienMotDePasse,

            @NotBlank(message = "Le nouveau mot de passe est obligatoire")
            @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
            String nouveauMotDePasse
    ) {}

    // ═══════════════════════════════════════════════════════════════════════════
    // RESPONSES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Réponse d'authentification avec tokens
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            UUID userId,
            String email,
            String nom,
            String prenom,
            String nomComplet,
            RoleUser role,
            String roleLibelle,
            StatutUser statut,
            Boolean emailVerifie,
            String message
    ) {
        public static AuthResponse of(
                String accessToken, String refreshToken, Long expiresIn,
                UUID userId, String email, String nom, String prenom,
                RoleUser role, StatutUser statut, Boolean emailVerifie
        ) {
            return new AuthResponse(
                    accessToken, refreshToken, "Bearer", expiresIn,
                    userId, email, nom, prenom, prenom + " " + nom,
                    role, role.getLibelle(), statut, emailVerifie, null
            );
        }

        public AuthResponse withMessage(String message) {
            return new AuthResponse(
                    accessToken, refreshToken, tokenType, expiresIn,
                    userId, email, nom, prenom, nomComplet,
                    role, roleLibelle, statut, emailVerifie, message
            );
        }
    }

    /**
     * Réponse utilisateur (sans tokens)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UserResponse(
            UUID id,
            String email,
            String nom,
            String prenom,
            String nomComplet,
            String telephone,
            RoleUser role,
            String roleLibelle,
            StatutUser statut,
            String statutLibelle,
            Boolean emailVerifie,
            String avatar,
            String adresse,
            LocalDateTime derniereConnexion,
            LocalDateTime createdAt
    ) {
        public static UserResponse from(sn.sunufarmasi.user.entity.User user) {
            return new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getNom(),
                    user.getPrenom(),
                    user.getNomComplet(),
                    user.getTelephone(),
                    user.getRole(),
                    user.getRole().getLibelle(),
                    user.getStatut(),
                    user.getStatut().getLibelle(),
                    user.getEmailVerifie(),
                    user.getAvatar(),
                    user.getAdresse(),
                    user.getDerniereConnexion(),
                    user.getCreatedAt()
            );
        }
    }
}
