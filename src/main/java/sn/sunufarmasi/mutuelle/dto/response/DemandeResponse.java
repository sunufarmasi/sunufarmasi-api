package sn.sunufarmasi.mutuelle.dto.response;

import sn.sunufarmasi.mutuelle.enums.StatutDemande;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DemandeResponse(
        UUID id,
        String numero,
        UUID mutuelleId,
        String mutuelleNom,
        UUID adherentId,
        String adherentNom,
        String numeroAdherent,
        UUID venteId,
        String numeroVente,
        StatutDemande statut,
        String statutLibelle,
        LocalDateTime dateDemande,
        LocalDateTime dateSoumission,
        LocalDateTime dateReponse,
        LocalDate datePaiement,
        BigDecimal montantVente,
        BigDecimal montantDemande,
        BigDecimal tauxApplique,
        BigDecimal montantAccepte,
        BigDecimal montantRejete,
        BigDecimal montantPaye,
        BigDecimal resteAPayer,
        String numeroOrdonnance,
        String medecinPrescripteur,
        String referencePaiement,
        String modePaiement,
        String referenceMutuelle,
        String motifRejet,
        boolean estPayee
) {}
