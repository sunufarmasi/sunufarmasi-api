package sn.sunufarmasi.pharmacie.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import sn.sunufarmasi.pharmacie.enums.TypePharmacien;

/**
 * DTO pour la modification d'un pharmacien par l'admin
 */
public record UpdatePharmacienRequest(
    String nom,
    String prenom,

    @Email(message = "Format email invalide")
    String email,

    @Pattern(regexp = "^\\+221[0-9]{9}$", message = "Format téléphone invalide: +221XXXXXXXXX")
    String telephone,

    String numeroOrdreNational,
    String universiteFormation,
    Integer anneeDiplome,
    TypePharmacien type,
    Boolean actif,

    @Pattern(regexp = "^[MF]$", message = "Le sexe doit être M ou F")
    String sexe
) {}
