package sn.sunufarmasi.pharmacie.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO Request pour la connexion d'un pharmacien
 *
 * @author WeCan
 * @since 1.0.0
 */
public record LoginPharmacienRequest(

        @NotBlank(message = "Le téléphone est obligatoire")
        String telephone

) {}