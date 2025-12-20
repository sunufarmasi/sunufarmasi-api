package sn.sunufarmasi.mutuelle.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour traiter une demande (accepter/rejeter/payer)
 */
public record TraiterDemandeRequest(

        @NotBlank(message = "L'action est obligatoire")
        String action,                            // ACCEPTER, REJETER, PAYER

        @DecimalMin("0") BigDecimal montant,      // Montant accepté ou payé

        String referenceMutuelle,
        String motifRejet,
        @Size(max = 1000) String observations,

        // Pour paiement
        String referencePaiement,
        String modePaiement,
        LocalDate datePaiement

) {}
