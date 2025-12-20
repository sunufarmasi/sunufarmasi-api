package sn.sunufarmasi.employe.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO Request pour la modification d'un employé
 *
 * @author WeCan
 * @since 1.0.0
 */
public record UpdateEmployeRequest(

        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom,

        @Pattern(regexp = "^\\+221[0-9]{9}$", message = "Format téléphone invalide (ex: +221771234567)")
        String telephone,

        @Email(message = "Format email invalide")
        @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères")
        String email,

        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateNaissance,

        @Pattern(regexp = "^[MF]$", message = "Le sexe doit être M ou F")
        String sexe,

        @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères")
        String adresse,

        @Size(max = 100, message = "Le poste ne doit pas dépasser 100 caractères")
        String poste,

        LocalDate dateFinContrat,

        @Size(max = 500, message = "L'URL de la photo ne doit pas dépasser 500 caractères")
        String photoUrl

) {}
