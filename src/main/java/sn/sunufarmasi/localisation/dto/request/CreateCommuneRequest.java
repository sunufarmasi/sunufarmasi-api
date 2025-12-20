package sn.sunufarmasi.localisation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.sunufarmasi.localisation.enums.TypeCommune;

import java.util.UUID;

/**
 * DTO création Commune
 */
public record CreateCommuneRequest(
    @NotNull(message = "Le département est obligatoire")
    UUID departementId,

    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20, message = "Le code ne peut dépasser 20 caractères")
    String code,

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut dépasser 100 caractères")
    String nom,

    TypeCommune type,
    String codePostal,
    Long population,
    Double superficie,
    Double latitude,
    Double longitude,
    Integer altitude,
    Integer ordre,
    Boolean zoneUrbaine,
    String arrondissement
) {}
