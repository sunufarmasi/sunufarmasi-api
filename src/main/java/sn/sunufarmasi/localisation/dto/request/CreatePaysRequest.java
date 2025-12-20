package sn.sunufarmasi.localisation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO création Pays
 */
public record CreatePaysRequest(
    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 10, message = "Le code ne peut dépasser 10 caractères")
    String code,

    @NotBlank(message = "Le code ISO2 est obligatoire")
    @Size(min = 2, max = 2, message = "Le code ISO2 doit faire 2 caractères")
    String codeIso2,

    @Size(min = 3, max = 3, message = "Le code ISO3 doit faire 3 caractères")
    String codeIso3,

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut dépasser 100 caractères")
    String nom,

    String nomEn,
    String capitale,
    String indicatifTelephonique,
    String devise,
    String fuseauHoraire,
    String drapeau
) {}
