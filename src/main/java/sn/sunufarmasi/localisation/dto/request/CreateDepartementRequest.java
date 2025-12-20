package sn.sunufarmasi.localisation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO création Département
 */
public record CreateDepartementRequest(
    @NotNull(message = "La région est obligatoire")
    UUID regionId,

    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20, message = "Le code ne peut dépasser 20 caractères")
    String code,

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut dépasser 100 caractères")
    String nom,

    String chefLieu,
    Long population,
    Double superficie,
    Double latitude,
    Double longitude,
    Integer ordre
) {}
