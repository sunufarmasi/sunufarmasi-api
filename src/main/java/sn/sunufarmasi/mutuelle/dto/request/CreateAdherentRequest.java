package sn.sunufarmasi.mutuelle.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour créer un adhérent
 */
public record CreateAdherentRequest(

        @NotNull(message = "La mutuelle est obligatoire")
        UUID mutuelleId,

        @NotBlank(message = "Le numéro d'adhérent est obligatoire")
        @Size(max = 50) String numeroAdherent,

        @Size(max = 50) String numeroCarte,
        String typeBeneficiaire,
        UUID adherentPrincipalId,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100) String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100) String prenom,

        LocalDate dateNaissance,
        String sexe,
        @Size(max = 20) String numeroCni,

        String telephone,
        @Email String email,
        @Size(max = 500) String adresse,

        @Size(max = 200) String employeur,
        @Size(max = 50) String matriculeEmploye,

        @DecimalMin("0") @DecimalMax("100") BigDecimal tauxCouverture,
        @DecimalMin("0") BigDecimal plafondAnnuel,

        LocalDate dateAdhesion,
        LocalDate dateFinDroits

) {}
