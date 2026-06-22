package sn.sunufarmasi.pharmacie.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO Request pour l'inscription d'un pharmacien
 * Seuls nom, prénom et téléphone sont obligatoires (création admin depuis fichier CSV).
 */
public record RegisterPharmacienRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^\\+221[0-9]{9}$", message = "Format téléphone invalide (ex: +221771234567)")
        String telephone,

        // Optionnel — pas toujours disponible lors de la saisie admin
        @Email(message = "Format email invalide")
        @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères")
        String email,

        // Optionnel
        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateNaissance,

        // Optionnel — défaut M si absent
        @Pattern(regexp = "^[MF]$", message = "Le sexe doit être M ou F")
        String sexe,

        // Optionnel
        @Size(max = 50, message = "Le numéro d'ordre ne doit pas dépasser 50 caractères")
        String numeroOrdreNational,

        // Optionnel
        @Size(max = 200, message = "L'université ne doit pas dépasser 200 caractères")
        String universiteFormation,

        // Optionnel
        @Min(value = 1950, message = "L'année de diplôme doit être >= 1950")
        @Max(value = 2050, message = "L'année de diplôme doit être <= 2050")
        Integer anneeDiplome

) {}
