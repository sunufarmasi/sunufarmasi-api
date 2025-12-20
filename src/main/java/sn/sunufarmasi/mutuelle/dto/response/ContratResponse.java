package sn.sunufarmasi.mutuelle.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContratResponse(
        UUID id,
        UUID mutuelleId,
        String mutuelleNom,
        String mutuelleCode,
        String numeroContrat,
        String numeroConventionnement,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal tauxCouverture,
        BigDecimal tauxEffectif,
        BigDecimal plafondMensuel,
        BigDecimal plafondAnnuel,
        Boolean tiersPayantAutorise,
        BigDecimal montantAvanceMax,
        Boolean estActif,
        boolean estValide
) {}
