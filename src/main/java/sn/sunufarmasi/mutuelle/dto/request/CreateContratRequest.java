package sn.sunufarmasi.mutuelle.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour créer un contrat pharmacie-mutuelle
 */
public record CreateContratRequest(

        @NotNull(message = "La mutuelle est obligatoire")
        UUID mutuelleId,

        String numeroContrat,
        String numeroConventionnement,

        @NotNull(message = "La date de début est obligatoire")
        LocalDate dateDebut,

        LocalDate dateFin,
        Boolean renouvellementAuto,

        @DecimalMin("0") @DecimalMax("100") BigDecimal tauxCouverture,
        @DecimalMin("0") BigDecimal plafondMensuel,
        @DecimalMin("0") BigDecimal plafondAnnuel,
        @Min(0) Integer delaiPaiementJours,

        String frequenceFacturation,
        @Min(1) @Max(31) Integer jourFacturation,
        @Email String emailFacturation,

        Boolean tiersPayantAutorise,
        @DecimalMin("0") BigDecimal montantAvanceMax,

        @Size(max = 1000) String notes

) {}
