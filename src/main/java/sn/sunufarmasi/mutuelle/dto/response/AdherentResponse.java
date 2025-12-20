package sn.sunufarmasi.mutuelle.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AdherentResponse(
        UUID id,
        UUID mutuelleId,
        String mutuelleNom,
        String numeroAdherent,
        String numeroCarte,
        String typeBeneficiaire,

        String nom,
        String prenom,
        String nomComplet,
        LocalDate dateNaissance,
        String sexe,

        String telephone,
        String email,

        String employeur,
        String matriculeEmploye,

        BigDecimal tauxCouverture,
        BigDecimal tauxEffectif,
        BigDecimal plafondAnnuel,
        BigDecimal consommationAnnuelle,
        BigDecimal resteAConsommer,

        LocalDate dateAdhesion,
        LocalDate dateFinDroits,
        Boolean estActif,
        boolean droitsOuverts,
        boolean plafondAtteint
) {}
