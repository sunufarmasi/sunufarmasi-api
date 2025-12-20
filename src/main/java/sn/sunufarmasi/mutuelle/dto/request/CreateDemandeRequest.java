package sn.sunufarmasi.mutuelle.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour créer une demande de remboursement
 */
public record CreateDemandeRequest(

        @NotNull(message = "La vente est obligatoire")
        UUID venteId,

        @NotNull(message = "L'adhérent est obligatoire")
        UUID adherentId,

        @Size(max = 50) String numeroOrdonnance,
        @Size(max = 200) String medecinPrescripteur,
        LocalDate dateOrdonnance,

        @Size(max = 1000) String notes

) {}
