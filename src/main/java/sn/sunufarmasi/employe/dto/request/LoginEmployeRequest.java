package sn.sunufarmasi.employe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO Request pour la connexion d'un employé
 *
 * @author WeCan
 * @since 1.0.0
 */
public record LoginEmployeRequest(

        @NotBlank(message = "Le username est obligatoire")
        @Size(max = 50, message = "Le username ne doit pas dépasser 50 caractères")
        String username,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(max = 100, message = "Le mot de passe ne doit pas dépasser 100 caractères")
        String motDePasse

) {}
