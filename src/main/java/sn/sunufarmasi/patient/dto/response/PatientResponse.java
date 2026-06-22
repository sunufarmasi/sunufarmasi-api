package sn.sunufarmasi.patient.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO Response pour Patient
 * Tous les patients ont un compte complet (email + password)
 *
 * @author WeCan
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatientResponse(
        String id,
        String email,
        String telephone,
        String nomComplet,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dateNaissance,

        String sexe,
        String adresse,
        String photoUrl,
        CommuneSimpleDto commune,
        boolean emailVerified,
        boolean telephoneVerified,
        boolean actif,
        boolean premiumActif,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dateFinPremium,

        Integer montantPremium,
        String referencePaiement,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime lastLoginAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {
    /**
     * DTO simplifié pour la commune
     */
    public record CommuneSimpleDto(
            String id,
            String nom,
            String departement,
            String region,
            CoordinatesDto coordinates
    ) {
        public record CoordinatesDto(
                Double latitude,
                Double longitude
        ) {}
    }

    /**
     * Constructeur simplifié sans commune ni premium
     */
    public PatientResponse(
            String id, String email, String telephone, String nomComplet,
            LocalDate dateNaissance, String sexe, String adresse, String photoUrl,
            boolean emailVerified, boolean telephoneVerified, boolean actif,
            LocalDateTime lastLoginAt, LocalDateTime createdAt
    ) {
        this(id, email, telephone, nomComplet, dateNaissance, sexe, adresse, photoUrl,
                null, emailVerified, telephoneVerified, actif, false, null, 0, null, lastLoginAt, createdAt);
    }
}