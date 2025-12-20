package sn.sunufarmasi.employe.dto.request;

import jakarta.validation.constraints.NotNull;
import sn.sunufarmasi.employe.enums.TypePermission;

import java.util.Set;

/**
 * DTO Request pour la modification des permissions d'un employé
 *
 * @author WeCan
 * @since 1.0.0
 */
public record UpdatePermissionsRequest(

        @NotNull(message = "Les permissions sont obligatoires")
        Set<TypePermission> permissions

) {}
